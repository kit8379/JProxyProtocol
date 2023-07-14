# JProxyProtocol

A simple Java application that acts as a TCP reverse proxy and supports the proxy protocol.

## Building the Application

To build this application, follow the steps:

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

