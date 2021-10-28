# Lambda Dropping Privileges (proof-of-concept) - WORK IN PROGRESS
**This is test code, please don't use for production**

This project contains source code and supporting files for a serverless application that drops the privileges of a Lambda function by [1] overwriting the environment variables in the Lambda process with other AWS credentials or [2] assuming the IAM Role passed by the caller and overwriting the environment variables.

There is sample event json file for testing that passed IAM credentials
```bash
sam build
sam local invoke -e events/event_setcreds.json
```
