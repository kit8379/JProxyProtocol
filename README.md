# JProxyProtocol

JProxyProtocol is a simple Java application that acts as a TCP reverse proxy and supports the Proxy Protocol.

## Building the Application

To build this application, follow these steps:

1. Make sure you have Java Development Kit (JDK) installed. If not, download and install it from the official Oracle website.

2. Navigate to the directory containing the source files in a terminal or command prompt.

3. Run the following command to compile the source code:
    ```
    javac JProxyProtocol.java
    ```

4. Now, to pack this compiled class into a JAR file, run the following command:
    ```
    jar cvfe JProxyProtocol.jar JProxyProtocol *.class
    ```
This will create a JAR file named 'JProxyProtocol.jar' in the same directory.

## Running the Application

To run the application, use the following command:
```
java -jar JProxyProtocol.jar
```

This command will start the JProxyProtocol application, which will listen on the port specified in the config.properties file and forward the incoming traffic to the specified target IP and port with Proxy Protocol v1 headers.

## Stopping the Application

To stop the application, type stop into the console where the application is running.

## Overview

This application listens for incoming connections on a specified port and forwards these connections to a target server. It also writes Proxy Protocol v1 headers, thereby allowing the target server to log client IPs accurately, even when the connections are proxied.

## Usage

This proxy can be utilized in multiple situations, such as load balancing, SSL termination, and concealing the IP address of a backend server. However, it's primarily intended for scenarios where it's crucial for the backend server to know the original client IP, such as certain logging or authentication scenarios.

# Limitations

The current implementation has several limitations:

1. It only supports the Proxy Protocol v1, which uses a human-readable header format. The binary format used in v2 is not supported.
2. It only supports TCP traffic, not UDP.
3. There's no SSL/TLS support, so it cannot be used for SSL termination or to proxy HTTPS traffic.
4. The proxy does not handle connection load balancing

# Future Improvements

Potential future improvements could include:

1. Support for Proxy Protocol v2. This would allow the proxy to be used with more servers and services, as v2 has more widespread support and can carry more information.
2. SSL/TLS support, which would allow the proxy to handle HTTPS traffic or terminate SSL connections, offloading this CPU-intensive task from the backend server.
3. Load balancing features, which would allow the proxy to distribute incoming connections across multiple backend servers for improved performance and fault tolerance.

This application is primarily intended for learning purposes. Thoroughly test it before using in a production environment, considering its limitations and potential security implications.
    
    
    