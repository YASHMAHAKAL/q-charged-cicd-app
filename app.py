#!/usr/bin/env python3
import os

import aws_cdk as cdk

from q_charged_cicd.q_charged_cicd_stack import QChargedCicdStack
from q_charged_cicd.pipeline_stack import PipelineStack


app = cdk.App()

# Infrastructure Stack
QChargedCicdStack(app, "QChargedCicdStack",
    env=cdk.Environment(account=os.getenv('CDK_DEFAULT_ACCOUNT'), region=os.getenv('CDK_DEFAULT_REGION')),
)

# CI/CD Pipeline Stack
PipelineStack(app, "PipelineStack",
    env=cdk.Environment(account=os.getenv('CDK_DEFAULT_ACCOUNT'), region=os.getenv('CDK_DEFAULT_REGION')),
)

app.synth()
