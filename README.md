# Node Manager Provisioner

A Kubernetes operator for managing AWS EC2 instances and Auto Scaling Groups (ASG) using custom resources. This operator simplifies the deployment and management of EC2 instances within a Kubernetes environment.

## Features

- **Instance Management**: Create and manage EC2 instances with custom configurations
- **Auto Scaling**: Support for Auto Scaling Groups with mixed instance policies
- **Custom Resources**: Define instance configurations using Kubernetes custom resources
- **AWS Integration**: Seamless integration with AWS services (EC2, Auto Scaling, IAM)
- **Flexible Configuration**: Support for various EC2 instance types, AMIs, and networking configurations

## Prerequisites

- Kubernetes cluster (EKS recommended)
- AWS credentials configured with appropriate permissions
- kubectl configured to communicate with your cluster
- Java 11 or later
- Maven

## Installation

### 1. Deploy the Operator

```bash
kubectl apply -f deploy/crds/aws.provisioner.app_instancegroups_crd.yaml
kubectl apply -f deploy/operator.yaml
```

### 2. Configure AWS Credentials

Create a Kubernetes secret with your AWS credentials:

```bash
kubectl create secret generic aws-credentials \
  --from-literal=aws_access_key_id=YOUR_AWS_ACCESS_KEY \
  --from-literal=aws_secret_access_key=YOUR_AWS_SECRET_KEY \
  -n provisioner
```

## Usage

### Creating an Instance Group

Create a YAML file (e.g., `instancegroup.yaml`) with your instance group configuration:

```yaml
apiVersion: aws.provisioner.app/v1alpha1
kind: InstanceGroup
metadata:
  name: example-instance-group
  namespace: provisioner
spec:
  name: "example-asg"
  region: "us-west-2"
  amiId: "ami-0c55b159cbfafe1f0"
  vpcZoneIdentifier: "subnet-12345678"
  minSize: 1
  maxSize: 3
  desiredCapacity: 2
  instanceType: "t3.medium"
  volumeSize: 30
  keyName: "my-keypair"
  securityGroupId: "sg-12345678"
  iamInstanceProfile: "arn:aws:iam::123456789012:instance-profile/example-role"
  mixedInstancesPolicy: true
  mixedInstanceTypes: 
    - "t3.medium"
    - "t3a.medium"
  userData: |
    #!/bin/bash
    echo "Custom user data script here"
```

Apply the configuration:

```bash
kubectl apply -f instancegroup.yaml
```

### Verifying the Instance Group

Check the status of your instance group:

```bash
kubectl get instancegroup -n provisioner
kubectl describe instancegroup example-instance-group -n provisioner
```

## Configuration Options

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| name | string | Yes | Name of the Auto Scaling Group |
| region | string | Yes | AWS region where resources will be created |
| amiId | string | Yes | AMI ID to use for instances |
| vpcZoneIdentifier | string | Yes | Comma-separated list of subnet IDs |
| minSize | int | Yes | Minimum number of instances |
| maxSize | int | Yes | Maximum number of instances |
| desiredCapacity | int | Yes | Desired number of instances |
| instanceType | string | Yes | Default EC2 instance type |
| volumeSize | int | Yes | Root volume size in GB |
| keyName | string | Yes | SSH key pair name |
| securityGroupId | string | Yes | Security group ID |
| iamInstanceProfile | string | Yes | IAM instance profile ARN |
| mixedInstancesPolicy | boolean | No | Enable mixed instances policy (default: false) |
| mixedInstanceTypes | []string | No | List of instance types for mixed instances policy |
| userData | string | No | User data script to run on instance launch |
| tags | map | No | Additional tags to apply to resources |

## Development

### Building the Operator

```bash
mvn clean package
```

### Running Locally

```bash
mvn spring-boot:run
```

### Testing

```bash
mvn test
```

## License

[Specify License]

## Contributing

[Contribution guidelines]

## Support

[Support information]
