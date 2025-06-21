# KUKE-Board Project Structure and Operation Principles

## Table of Contents
1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Technology Stack](#technology-stack)
4. [Common Modules](#common-modules)
5. [Service Modules](#service-modules)
6. [Infrastructure Components](#infrastructure-components)
7. [Data Flow Diagram](#data-flow-diagram)
8. [Detailed Process Description](#detailed-process-description)

## Overview

KUKE-Board is a bulletin board system built on a microservice architecture (MSA). The system consists of several independent services and utilizes MySQL, Kafka, and Redis to implement efficient data processing and inter-service communication. This document explains the relationships between modules in the `common` and `service` folders and their interactions with infrastructure components.

## Project Structure

The project consists of two main folders:

```
kuke-board/
├── common/                 # Common functionality modules
│   ├── event               # Event definition and processing
│   ├── snowflake           # Distributed ID generator
│   ├── data-serializer     # Data serialization/deserialization
│   └── outbox-message-relay # Transaction stability guarantee
└── service/                # Individual service modules
    ├── article             # Article management
    ├── comment             # Comment management
    ├── like                # Like functionality
    ├── view                # View count management
    ├── hot-article         # Popular article processing
    └── article-read        # Article reading optimization
```

## Execution Order
 - ArticleApplication
 - CommentApplication
 - LikeApplication
 - ViewApplication
 - ArticleReadApplication
 - HotArticleApplication

## Technology Stack

KUKE-Board uses the following key technologies:

1. **Jakarta EE**: Java-based enterprise application development standard
2. **Spring Data JPA**: Simplification of data access layer
3. **Spring MVC**: Web layer processing
4. **Lombok**: Code simplification utility
5. **Java 21**: Utilizing the latest Java features

### Infrastructure Components
- **MySQL**: Persistent data storage
- **Kafka**: Event-based communication message broker
- **Redis**: Caching and temporary data storage

## Common Modules

The Common folder contains functionality used across multiple services. The role of each module is as follows:

### 1. event
- **Role**: Defining event structures for inter-service communication
- **Main Features**: Event message format definition, event handling interface provision
- **Related Infrastructure**: Direct integration with Kafka for event publishing and subscription

### 2. snowflake
- **Role**: Generating unique IDs in a distributed environment
- **Main Features**: Time-based unique ID generation algorithm
- **Core Value**: Guaranteeing duplicate-free ID generation in distributed environments

### 3. data-serializer
- **Role**: Data serialization/deserialization functionality
- **Main Features**: JSON conversion, object serialization support
- **Related Infrastructure**: Format conversion for Kafka messages and Redis cache data

### 4. outbox-message-relay
- **Role**: Ensuring reliability of event publishing
- **Main Features**: Atomically processing database transactions and event publishing
- **Related Infrastructure**: Ensuring consistency between MySQL and Kafka
- **Operation Method**:
    - When data changes, messages are stored in the outbox table along with MySQL transactions
    - A separate process periodically reads unpublished messages from the outbox table and publishes them to Kafka
    - Upon successful message publication, the status is updated in the outbox table

## Service Modules

The Service folder consists of microservices, each responsible for independent functionality:

### 1. article
- **Role**: Article CRUD processing
- **Main Features**: Article creation, modification, deletion, retrieval
- **Database**: MySQL (permanent article storage)
- **Cache**: Redis (caching frequently viewed articles)
- **Event Publishing**: Publishing Kafka events when articles are created/modified/deleted

### 2. comment
- **Role**: Providing comment functionality
- **Main Features**: Comment creation, modification, deletion, retrieval
- **Database**: MySQL (permanent comment storage)
- **Cache**: Redis (comment list caching)
- **Event Publishing**: Publishing comment-related events

### 3. like
- **Role**: Article/comment like functionality
- **Main Features**: Adding/canceling likes, aggregating like counts
- **Database**: MySQL (permanent like data storage)
- **Cache**: Redis (real-time like count management)
- **Event Publishing**: Publishing like status change events

### 4. view
- **Role**: Article view count management
- **Main Features**: Increasing view counts, view count statistics
- **Database**: MySQL (storing view count statistics)
- **Cache**: Redis (preventing duplicate views and managing real-time view counts)
- **Event Publishing**: Publishing view count change events

### 5. hot-article
- **Role**: Popular article selection
- **Main Features**: Calculating popular articles based on views and likes
- **Database**: MySQL (storing popular article lists)
- **Message Subscription**: Subscribing to like and view events to calculate popularity
- **Cache**: Redis (caching popular article lists)

### 6. article-read
- **Role**: Article retrieval dedicated service
- **Main Features**: Providing optimized article reading functionality
- **Database**: MySQL (read-only optimized queries)
- **Cache**: Redis (caching frequently viewed articles and statistical data)
- **Message Subscription**: Subscribing to article, view count, and like events to keep data up-to-date

## Infrastructure Components

### MySQL
- **Role**: Persistent data storage
- **Usage Areas**:
    - Storing unique data for each service
    - Transaction management
    - Temporary message storage for Outbox pattern

### Kafka
- **Role**: Event-centered inter-service communication
- **Usage Areas**:
    - Event publishing/subscription between services
    - Real-time propagation of data changes
    - Providing system scalability and fault tolerance

### Redis
- **Role**: High-performance caching and temporary data storage
- **Usage Areas**:
    - Caching frequently accessed data
    - Managing real-time counters (view counts, like counts)
    - Session data management
    - Preventing duplicate requests (e.g., preventing duplicate views)

## Data Flow Diagram

Below is a diagram showing the data flow of major processes and interactions between infrastructure components:
![Data Flow Diagram](./asset/sequence-diagram-full.svg)

## Detailed Process Description

### Article Creation and Propagation Process

1. When a user creates an article, it is processed by the `article` service.
2. The `article` service uses the `snowflake` module to generate a unique ID.
3. Article data is stored in MySQL.
4. Simultaneously, an event is stored in the outbox table in MySQL via the `outbox-message-relay` module.
5. `outbox-message-relay` checks for unpublished messages in the outbox table through a separate scheduler and publishes them to Kafka.
6. Messages are serialized via `data-serializer` and sent to Kafka.
7. Services such as `article-read` and `hot-article` subscribe to events from Kafka to update their data.
8. The `article-read` service caches frequently viewed articles in Redis to provide fast response times.

### Like Processing Process

1. When a user likes an article, it is processed by the `like` service.
2. Like information is stored in MySQL.
3. Simultaneously, the real-time like count is increased in Redis.
4. A like event is published to Kafka via `outbox-message-relay`.
5. The `hot-article` service subscribes to this event and updates the popular article score in Redis.
6. The `article-read` service subscribes to like events and updates cached article information.

### View Count Processing Process

1. When a user views an article, the `article-read` service provides article information.
    - It first checks the Redis cache, and if not found, retrieves from MySQL.
2. Simultaneously, a view count increase request is sent to the `view` service.
3. The `view` service uses Redis to check for duplicate views by user.
4. If not a duplicate, it increases the view count and records it in MySQL.
5. The view count increase event is published via Kafka, and the `hot-article` service subscribes to it to update popularity scores.

### Popular Article Calculation Process

1. The `hot-article` service subscribes to like, view count, and comment events from Kafka.
2. When events occur, it updates article scores in Redis in real-time.
3. Periodically, it selects high-scoring articles and stores the popular article list in MySQL.
4. The popular article list is cached in Redis again for quick retrieval.

The key advantages of this structure are:

1. **Scalability**: Each service can be scaled independently.
2. **Fault Isolation**: Problems in one service do not affect the entire system.
3. **Data Consistency**: `outbox-message-relay` ensures consistency between database transactions and event publishing.
4. **Performance Optimization**: Caching with Redis minimizes response time for frequently accessed data.
5. **Real-time**: Event-based communication through Kafka enables real-time propagation of data changes.