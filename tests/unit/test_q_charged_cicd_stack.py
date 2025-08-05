import aws_cdk as core
import aws_cdk.assertions as assertions

from q_charged_cicd.q_charged_cicd_stack import QChargedCicdStack

# example tests. To run these tests, uncomment this file along with the example
# resource in q_charged_cicd/q_charged_cicd_stack.py
def test_sqs_queue_created():
    app = core.App()
    stack = QChargedCicdStack(app, "q-charged-cicd")
    template = assertions.Template.from_stack(stack)

#     template.has_resource_properties("AWS::SQS::Queue", {
#         "VisibilityTimeout": 300
#     })
