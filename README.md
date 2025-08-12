# Spring Cassandra Microservice

A nearly production-ready Spring Boot microservice template demonstrating enterprise-grade patterns and practices for Apache Cassandra integration with modern Java stack.

## 🚀 Features

### Core Technologies
- **Java 21** with modern features and performance improvements
- **Spring Boot 3.x** with Spring MVC and Jakarta EE
- **Apache Cassandra** with DataStax Java Driver 4.x
- **Hexagonal Architecture** (Ports & Adapters pattern)
- **Multi-module Gradle** project structure

### Production-Ready Features
- ✅ **Database Migration System** - Custom Cassandra migration framework
- ✅ **Code Quality** - PMD static analysis integration
- ✅ **Testing Strategy** - Unit, integration, and testcontainers setup
- ✅ **Docker Support** - Multi-stage builds and development containers
- ✅ **Monitoring** - Structured logging with SLF4J/Logback
- ✅ **Configuration Management** - Environment-specific configurations
- ✅ **API Documentation** - OpenAPI 3.0 specification

## 🏗 Architecture

This project follows **Hexagonal Architecture** principles with clear separation of concerns:

- **domain**: Core domain logic with ports (interfaces), models, exceptions, and services
- **adapters/input/web**: Web adapters implementing OpenAPI-generated interfaces
- **app**: Application bootstrap and infrastructure concerns (migrations, main class)
- **api/specification**: OpenAPI 3.0 specification in YAML format
- **api/generated**: Generated code from OpenAPI specification
- **integration-tests**: Comprehensive integration tests using Testcontainers
- **infrastructure**: Terraform configurations for multi-environment deployments