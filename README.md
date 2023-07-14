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

   ```
   java -jar JProxyProtocol.jar
   ```


This command will start the JProxyProtocol application, which will listen on port 25565 and forward the incoming traffic to localhost:30000 with Proxy Protocol v1 headers.

## Stopping the Application

To stop the application, simply terminate the running process in your terminal or command prompt by pressing `Ctrl+C`.


