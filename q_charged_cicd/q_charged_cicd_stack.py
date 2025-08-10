from aws_cdk import (
    Stack,
    aws_ec2 as ec2,
    aws_eks as eks,
    aws_iam as iam,
    aws_ecr as ecr,
    aws_cognito as cognito,
    CfnOutput,
)
from constructs import Construct

class QChargedCicdStack(Stack):

    def __init__(self, scope: Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # VPC with public and private subnets across 2 AZs
        vpc = ec2.Vpc(
            self, "EksVpc",
            max_azs=2,
            subnet_configuration=[
                ec2.SubnetConfiguration(
                    name="Public",
                    subnet_type=ec2.SubnetType.PUBLIC,
                    cidr_mask=24
                ),
                ec2.SubnetConfiguration(
                    name="Private",
                    subnet_type=ec2.SubnetType.PRIVATE_WITH_EGRESS,
                    cidr_mask=24
                )
            ]
        )

        # EKS Cluster Service Role
        cluster_role = iam.Role(
            self, "ClusterRole",
            assumed_by=iam.ServicePrincipal("eks.amazonaws.com"),
            managed_policies=[
                iam.ManagedPolicy.from_aws_managed_policy_name("AmazonEKSClusterPolicy")
            ]
        )

        # EKS Cluster
        cluster = eks.CfnCluster(
            self, "ProdCluster",
            name="prod-cluster",
            version="1.28",
            role_arn=cluster_role.role_arn,
            resources_vpc_config=eks.CfnCluster.ResourcesVpcConfigProperty(
                subnet_ids=[subnet.subnet_id for subnet in vpc.private_subnets + vpc.public_subnets]
            )
        )

        # Node Group Role
        nodegroup_role = iam.Role(
            self, "NodeGroupRole",
            assumed_by=iam.ServicePrincipal("ec2.amazonaws.com"),
            managed_policies=[
                iam.ManagedPolicy.from_aws_managed_policy_name("AmazonEKSWorkerNodePolicy"),
                iam.ManagedPolicy.from_aws_managed_policy_name("AmazonEKS_CNI_Policy"),
                iam.ManagedPolicy.from_aws_managed_policy_name("AmazonEC2ContainerRegistryReadOnly")
            ]
        )

        # Managed Node Group
        nodegroup = eks.CfnNodegroup(
            self, "ManagedNodeGroup",
            cluster_name=cluster.name,
            node_role=nodegroup_role.role_arn,
            subnets=[subnet.subnet_id for subnet in vpc.private_subnets],
            instance_types=["t3.micro"],
            scaling_config=eks.CfnNodegroup.ScalingConfigProperty(
                min_size=1,
                max_size=3,
                desired_size=2
            )
        )
        nodegroup.add_dependency(cluster)

        # ECR Repositories
        product_repo = ecr.Repository(
            self, "ProductServiceRepo",
            repository_name="product-service"
        )
        
        review_repo = ecr.Repository(
            self, "ReviewServiceRepo",
            repository_name="review-service"
        )

        # Cognito User Pool
        user_pool = cognito.UserPool(
            self, "UserPool",
            user_pool_name="prod-user-pool"
        )
        
        user_pool_client = cognito.UserPoolClient(
            self, "UserPoolClient",
            user_pool=user_pool,
            user_pool_client_name="prod-client"
        )

        CfnOutput(self, "ClusterName", value=cluster.name)
        CfnOutput(self, "VpcId", value=vpc.vpc_id)
        CfnOutput(self, "ProductRepoUri", value=product_repo.repository_uri)
        CfnOutput(self, "ReviewRepoUri", value=review_repo.repository_uri)
        CfnOutput(self, "UserPoolId", value=user_pool.user_pool_id)
        CfnOutput(self, "UserPoolClientId", value=user_pool_client.user_pool_client_id)
