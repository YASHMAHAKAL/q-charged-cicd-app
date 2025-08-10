from aws_cdk import (
    Stack,
    aws_codepipeline as codepipeline,
    aws_codepipeline_actions as codepipeline_actions,
    aws_codebuild as codebuild,
    aws_iam as iam,
    CfnOutput,
)
from constructs import Construct

class PipelineStack(Stack):

    def __init__(self, scope: Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # Source artifact
        source_output = codepipeline.Artifact("SourceOutput")
        
        # Build artifacts
        build_output = codepipeline.Artifact("BuildOutput")
        test_output = codepipeline.Artifact("TestOutput")

        # CodeBuild project for Build stage
        build_project = codebuild.Project(
            self, "BuildProject",
            project_name="q-charged-build",
            build_spec=codebuild.BuildSpec.from_object({
                "version": "0.2",
                "phases": {
                    "pre_build": {
                        "commands": [
                            "echo Logging in to Amazon ECR...",
                            "cd application-source"
                        ]
                    },
                    "build": {
                        "commands": [
                            "echo Build started on `date`",
                            "mvn clean install -DskipTests"
                        ]
                    },
                    "post_build": {
                        "commands": [
                            "echo Build completed on `date`"
                        ]
                    }
                },
                "artifacts": {
                    "files": ["**/*"]
                }
            }),
            environment=codebuild.BuildEnvironment(
                build_image=codebuild.LinuxBuildImage.STANDARD_5_0,
                compute_type=codebuild.ComputeType.SMALL
            )
        )

        # CodeBuild project for Test stage
        test_project = codebuild.Project(
            self, "TestProject",
            project_name="q-charged-test",
            build_spec=codebuild.BuildSpec.from_object({
                "version": "0.2",
                "phases": {
                    "pre_build": {
                        "commands": [
                            "cd application-source"
                        ]
                    },
                    "build": {
                        "commands": [
                            "echo Test started on `date`",
                            "mvn test"
                        ]
                    },
                    "post_build": {
                        "commands": [
                            "echo Test completed on `date`"
                        ]
                    }
                }
            }),
            environment=codebuild.BuildEnvironment(
                build_image=codebuild.LinuxBuildImage.STANDARD_5_0,
                compute_type=codebuild.ComputeType.SMALL
            )
        )

        # CodeBuild project for Deploy stage (placeholder)
        deploy_project = codebuild.Project(
            self, "DeployProject",
            project_name="q-charged-deploy",
            build_spec=codebuild.BuildSpec.from_object({
                "version": "0.2",
                "phases": {
                    "build": {
                        "commands": [
                            "echo Deploy stage placeholder",
                            "echo This will deploy to EKS cluster"
                        ]
                    }
                }
            }),
            environment=codebuild.BuildEnvironment(
                build_image=codebuild.LinuxBuildImage.STANDARD_5_0,
                compute_type=codebuild.ComputeType.SMALL
            )
        )

        # CodePipeline AWS
        pipeline = codepipeline.Pipeline(
            self, "Pipeline",
            pipeline_name="q-charged-cicd-pipeline",
            stages=[
                codepipeline.StageProps(
                    stage_name="Source",
                    actions=[
                        codepipeline_actions.CodeStarConnectionsSourceAction(
                            action_name="GitHub_Source",
                            owner="YASHMAHAKAL",
                            repo="q-charged-cicd-app",
                            branch="master",
                            output=source_output,
                            connection_arn="arn:aws:codeconnections:us-east-1:831926626327:connection/e1d949bc-a83c-4a3a-a6ca-c60014b99c12"
                        )
                    ]
                ),
                codepipeline.StageProps(
                    stage_name="Build",
                    actions=[
                        codepipeline_actions.CodeBuildAction(
                            action_name="Build",
                            project=build_project,
                            input=source_output,
                            outputs=[build_output]
                        )
                    ]
                ),
                codepipeline.StageProps(
                    stage_name="Test",
                    actions=[
                        codepipeline_actions.CodeBuildAction(
                            action_name="Test",
                            project=test_project,
                            input=source_output
                        )
                    ]
                ),
                codepipeline.StageProps(
                    stage_name="Deploy",
                    actions=[
                        codepipeline_actions.CodeBuildAction(
                            action_name="Deploy_Placeholder",
                            project=deploy_project,
                            input=build_output
                        )
                    ]
                )
            ]
        )

        CfnOutput(self, "PipelineName", value=pipeline.pipeline_name)
        CfnOutput(self, "PipelineArn", value=pipeline.pipeline_arn)