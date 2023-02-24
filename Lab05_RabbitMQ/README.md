# Intro

[Source link:](https://www.rabbitmq.com/tutorials/tutorial-one-java.html)

## How to run
To make it work, several important steps:
1. from the source, download the three dependencies and copy past to the same level as `Recv.java` and `Send.java` (it is 2023 can you believe that...)
2. you will need to have rabbit queue running (via `brew services start rabbitmq`)
3. very likely the `javac` line in the source will fail. In that case, use the intelliJ run instead.