# RabbitMQ

## 一、基础介绍

### 1、什么是 MQ

**MQ：MessageQueue，消息队列**

这东西分两个部分来理解：

队列，是一种FIFO 先进先出的数据结构。

消息：在不同应用程序之间传递的数据。将消息以队列的形式存储起来，并且在不同的应用程序之间进行传递，这就成了MessageQueue



### 2、什么是消息队列

消息队列是一种在应用程序之间传递消息的技术。它提供了一种异步通信模式，允许应用程序在不同的时间处理消息。消息队列通常用于解耦应用程序，以便它们可以独立地扩展和修改。在消息队列中，消息发送者将消息发送到队列中，然后消息接收者从队列中接收消息。这种模式允许消息接收者按照自己的节奏处理消息，而不必等待消息发送者处理完消息。常见的消息队列包括RabbitMQ、Kafka和ActiveMQ等。

**解耦**：发送方和接收方不再直接依赖

**削峰填谷**：缓冲高并发请求，防止系统崩溃

**异步**：提升系统响应速度，如下单后异步发短信



### 3、优缺点

虽然 SpringBoot 已经提供了本地的事件驱动支持，不是给SpringBoot应用加上一些web接口，基于这些web接口不就可以将本地的这些系统事件以及自己产生的这些事件往外部应用推送，那这不就成了一个MQ服务。其实上面列出了MQ的的很多优点， 但是在具体使用MQ时，也会带来很多的缺点：

- 系统可用性降低：系统引入的外部依赖增多，系统的稳定性就会变差。一旦MQ宕机，对业务会产生影响。这就需要考虑如何保证MQ的高可用
- 系统复杂度提高：引入MQ后系统的复杂度会大大提高。以前服务之间可以进行同步的服务调用，引入MQ后，会变为异步调用，数据的链路就会变得更复杂。并且还会带来其他一些问题。比如：消息如何高效存储、如何定期维护、如何监控、如何溯源等等。如何保证消费不会丢失？不会被重复调用？怎么保证消息的顺序性等问题。
- 消息安全性问题：引入MQ后，消息需要在MQ中存储起来。这时就会带来很多网络造成的数据安全问题。比如如何快速保存海量消息？如何保证消息不丢失？不被重复处理？怎么保证消息的顺序性？如何保证消息事务完整等问题。



### 4、主流 MQ 产品

MQ通常用起来比较简单，但是实现上是非常复杂的。基本上MQ代表了业界高可用、高并发、高可扩展三高架构的所有设计精髓。在MQ长期发展过程中，诞生了很多MQ产品，但是有很多MQ产品都已经逐渐被淘汰了。比如早期的ZeroMQ,ActiveMQ等。目前最常用的MQ产品包括Kafka、RabbitMQ和RocketMQ

|          | 优点                             | 缺点                                                        | 使用场景             |
| -------- | -------------------------------- | ----------------------------------------------------------- | -------------------- |
| Kafka    | 吞吐量大、性能好、集群高可用     | 会丢失数据，功能比较单一                                    | 日志分析、大数据采集 |
| RabbitMQ | 消息可靠性高、功能全面           | 吞吐量比较低、消息积累会影响性能、erlang 语言不好定制       | 小规模场景           |
| RocketMQ | 高吞吐、高性能、高可用、功能全面 | 开源版功能不如云上版、官方文档比较简单、客户端仅仅支持 Java | 全场景               |

RabbitMQ的历史可以追溯到2006年，是一个非常老牌的MQ产品，使用非常广泛。同时期的很多MQ产品都已经逐渐被业界淘汰了，比如2003年诞生的ActiveMQ，2012年诞生的ZeroMQ，但是RabbitMQ却依然稳稳占据一席之地，足可见他的经典



## 二、快速上手

### 1、部署

#### 基于 Docker 安装

安装好 Docker 后

```bash
docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.11-management
```



#### 基于 RPM 安装

注意：**安装RabbitMQ之前需要先安装 Erlang 语言包**

```bash
rpm -ivh erlang-25.2.2-1.el9.x86_64.rpm
```

可以通过命令，查看是否安装成功

```bash
erl -version
```



**安装MQ**

```bash
rpm -ivh rabbitmq-server-3.11.10-1.el8.noarch.rpm
```



**常见的指令**

- service rabbitmq-server start ：启动Rabbitmq服务，启动应用之前要先启动服务
- rabbitmq-server -deched ：后台启动RabbitMQ应用
- rabbitmqctl start_app ：启动Rabbitmq
- rabbitmqctl stop ：关闭Rabbitmq
- rabbitmqctl status ：查看RabbitMQ服务状态



**启动RabbitMQ插件**

RabbitMQ提供了管理插件，可以快速使用RabbitMQ

```bash
rabbitmq-plugins enable rabbitmq_management
```

插件激活后，就可以访问 15672 端口，提供的默认用户名和密码是 `guest`

**注意：默认情况下只允许本地登录，远程访问无效**



**创建远程访问用户**

这时，通常都会创建一个管理员账号单独对RabbitMQ进行管理

```bash
# 创建 admin 用户
rabbitmqctl add_user admin admin

# 设置远程访问
rabbitmqctl set_permissions -p / admin "." "." ".*"

# 赋予管理员权限
rabbitmqctl set_user_tags admin administrator
```



### 2、理解 Exchange 与 Queue

Exchange和Queue是RabbitMQ中用来传递消息的核心组件

**创建一个消息队列**

![创建队列](.\images\创建队列.png)

创建完成后，选择这个 `test_01` 队列，就可以在页面上直接发送信息以及消息信息

从这里可以看到，RabbitMQ中的消息都是通过Queue队列传递的，这个Queue其实就是一个典型的FIFO的队列数据结构。而Exchange交换机则是用来辅助进行消息分发的。Exchange与Queue之间会建立一种绑定关系，通过绑定关系，Exchange交换机里发送的消息就可以分发到不同的Queue上



**交换机**

进入Exchanges菜单，可以看到针对每个虚拟机，RabbitMQ都预先创建了多个Exchange交换机

![交换机绑定队列](.\images\交换机绑定队列.png)

这里我们选择 `amq.direct` 交换机，进入交换机详情页，选择Binding，并将test_01队列绑定到这个交换机上

可以通过这个绑定的交换机发送信息

![交换机发送信息](.\images\交换机发送信息.png)

既然可以发送，也就可以进行查看，在进入绑定的队列中

![队列获取信息](.\images\队列获取信息.png)

Exchange交换机既然可以绑定一个队列，当然也可以绑定更多的队列。而Exchange的作用，就是将发送到Exchange的消息转发到绑定的队列上。在具体使用时，通常只有消息生产者需要与Exchange打交道。而消费者，则并不需要与Exchange打交道，只要从Queue中消费消息就可以了

另外，Exchange并不只是简单的将消息全部转发给Queue，在实际使用中，Exchange与Queue之间可以建立不同类型的绑定关系，然后通过一些不同的策略，选择将消息转发到哪些Queue上。这时候，Messaage上几个没有用上的参数，像Routing Key ,Headers，Properties这些参数就能派上用场了



### 3、理解 Connection 与 Channel

这两个概念实际上是跟客户端应用的对应关系。一个Connection可以理解为一个客户端应用。而一个应用可以创建多个Channel，用来与RabbitMQ进行交互

**搭建简单的 MQ 客户端**

**修改 pom.xml **

```xml
<dependency>
	<groupId>com.rabbitmq</groupId>
	<artifactId>amqp-client</artifactId>
	<version>5.9.0</version>
</dependency>
```

**测试案例**

```java
package com.xuenai.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ClientTest {

    private static final String HOST_NAME="175.178.210.249";
    //    AMQP 协议通信端口
    private static final int HOST_PORT=5672;
    private static final String QUEUE_NAME="test_01";
    public static final String USER_NAME="admin";
    public static final String PASSWORD="admin";
    public static final String VIRTUAL_HOST="/";
    
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setPort(HOST_PORT);
        factory.setUsername(USER_NAME);
        factory.setPassword(PASSWORD);
        factory.setVirtualHost(VIRTUAL_HOST);
        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();
        /**
         * 创建一个队列
         * 几个参数依次是: 队列名,durable是否实例化,exclusive:是否被独占,autoDelete:是否自动删除,arguments:参数
         * 如果 Broker 上没有改队列,则会自动创建
         * 但是如果有改队列,没有这些属性会报错
         */
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
//        每个worker同时只可以处理一个消息
        channel.basicQos(1);
//        回调函数,处理接收的消息
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                System.out.println("routingKey: " + routingKey);
                String type = properties.getContentType();
                System.out.println("contentType: " + type);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println("deliveryTag: " + deliveryTag);
                System.out.println("content: " + new String(body,"UTF-8"));
                channel.basicAck(deliveryTag,false);
            }
        };
//        消费数据
        channel.basicConsume(QUEUE_NAME,consumer);
    }
}

```

这个时候，只需要去网页端，发送信息即可在这个控制台获取到发送的信息



### 4、概念总结

#### Broker

一个搭建RabbitMQ Server的服务器称为Broker。这个并不是RabbitMQ特有的概念，但是却是几乎所有MQ产品通用的一个概念。未来如果需要搭建集群，就需要通过这些Broker来构建



#### Virtual Host

RabbitMQ出于服务器复用的想法，可以在一个RabbitMQ集群中划分出多个虚拟主机，每一个虚拟主机都有全套的基础服务组件，可以针对每个虚拟主机进行权限以及数据分配。不同虚拟主机之间是完全隔离的，如果不考虑资源分配的情况，一个虚拟主机就可以当成一个独立的RabbitMQ服务使用。



#### Connection

客户端与RabbitMQ进行交互，首先就需要建立一个TPC连接，这个连接就是Connection。既然是通道，那就需要尽量注意在停止使用时要关闭，释放资源



#### Channel

一旦客户端与RabbitMQ建立了连接，就会分配一个AMQP信道 Channel。每个信道都会被分配一个唯一的ID。也可以理解为是客户端与RabbitMQ实际进行数据交互的通道，我们后续的大多数的数据操作都是在信道 Channel 这个层面展开的

RabbitMQ为了减少性能开销，也会在一个Connection中建立多个Channel，这样便于客户端进行多线程连接，这些连接会复用同一个Connection的TCP通道，所以在实际业务中，对于Connection和Channel的分配也需要根据实际情况进行考量



#### Exhange

这是RabbitMQ中进行数据路由的重要组件。消息发送到RabbitMQ中后，会首先进入一个交换机，然后由交换机负责将数据转发到不同的队列中。RabbitMQ中有多种不同类型的交换机来支持不同的路由策略

交换机多用来与生产者打交道。生产者发送的消息通过Exchange交换机分配到各个不同的Queue队列上，而对于消息消费者来说，通常只需要关注自己感兴趣的队列就可以了



#### Queue

Queue是实际保存数据的最小单位。Queue不需要Exchange也可以独立工作，只不过通常在业务场景中，会增加Exchange实现更复杂的消息分配策略。Queue结构天生就具有FIFO的顺序，消息最终都会被分发到不同的Queue当中，然后才被消费者进行消费处理。这也是最近RabbitMQ功能变动最大的地方。最为常用的是经典队列Classic。RabbitMQ 3.8.X版本添加了Quorum队列，3.9.X又添加了Stream队列 



## 三、基础编程

### 1、获取连接

```java
ConnectionFactory factory = new ConnectionFactory();
factory.setHost(HOST_NAME);
factory.setPort(HOST_PORT);
factory.setUsername(USER_NAME);
factory.setPassword(PASSWORD);
factory.setVirtualHost(VIRTUAL_HOST);
Connection connection = factory.newConnection();
Channel channel = connection.createChannel();
```

通常情况下，在一个客户端里都只是创建一个Channel就可以了，因为一个Channel只要不关闭，是可以一直复用的。但是，如果你想要创建多个Channel，要注意一下Channel冲突的问题。

在创建channel时，可以在createChannel方法中传入一个分配的int参数channelNumber。这个ChannelNumber就会作为Channel的唯一标识。而RabbitMQ防止ChannelNumber重复的方式是：如果对应的Channel没有创建过，就会创建一个新的Channel。但是如果ChannelNumber已经创建过一个Channel了，这时就会返回一个null



### 2、声明 Exchange

```java
channel.exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete,Map<String, Object> arguments) throws IOException;
```

Exchange在消息收发过程中是一个可选的步骤，如果要使用就需要先进行声明。在声明Exchange时需要注意，如果Broker上没有对应的Exchange，那么RabbitMQ会自动创建一个新的交换机。但是如果Broker上已经有了这个Exchange，那么你声明时的这些参数需要与Broker上的保持一致。如果不一致就会报错。

声明Exchange时可以填入很多参数，对这些参数，你不用死记。实际上这些参数，包括最后的arguments中可以传入哪些参数，在管理控制台中都有。关键属性在页面上都有解释



### 3、声明 Queue

```java
channel.queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments);
```

这是应用开发过程中必须要声明的一个组件。与Exchange一样，如果你声明的Queue在Broker上不存在，RabbitMQ会创建一个新的队列。但是如果Broker上已经有了这个队列，那么声明的属性必须和Broker上的队列保持一致，否则也会报错

Durablility表示是否持久化。Durable选项表示会将队列的消息写入硬盘，这样服务重启后这些消息就不会丢失。而另外一个选项Transient表示不持久化，消息只在内存中流转。这样服务重启后这些消息就会丢失。当然这也意味着消息读写的效率会比较高。

但是Queue与Exchange不同的是， 队列类型并没有在API中体现。这是因为不同类型之间的Queue差距是很大的，无法用统一的方式来描述不同类型的队列。比如对于Quorum和Stream类型，根本就没有Durability和AutoDelete属性，他们的消息默认就是会持久化的。后面的属性参数也会有很大的区别。

唯一有点不同的是队列的Type属性。在客户端API中，目前并没有一个单独的字段来表示队列的类型。只能通过后面的arguments参数来区分不同的队列。

如果要声明一个Quorum队列，则只需要在后面的arguments中传入一个参数，**x-queue-type**，参数值设定为**quorum**

```java
Map<String,Object> params = new HashMap<>();
params.put("x-queue-type","quorum");
//声明Quorum队列的方式就是添加一个x-queue-type参数，指定为quorum。默认是classic
channel.queueDeclare(QUEUE_NAME, true, false, false, params);
```

如果要声明一个Stream队列，则 **x-queue-type**参数要设置为 **stream**

```java
Map<String,Object> params = new HashMap<>();
params.put("x-queue-type","stream");
params.put("x-max-length-bytes", 20_000_000_000L); // maximum stream size:20 GB
params.put("x-stream-max-segment-size-bytes", 100_000_000); // size of segment files: 100 MB
channel.queueDeclare(QUEUE_NAME, true, false, false, params);
```



### 4、声明 Exchange 与 Queue 的绑定关系

```java
channel.queueBind(String queue, String exchange, String routingKey) throws IOException;
```



## 四、常用的消息场景

**Utils**

```java
package com.xuenai.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQUtil {


    private static Connection connection;
    private static final String HOST_NAME="";
    private static final int HOST_PORT=5672;
    public static final String QUEUE_HELLO="hello";
    public static final String QUEUE_WORK="work";
    public static final String QUEUE_PUBLISH="publish";

    private RabbitMQUtil() {}

    public static Connection getConnection() throws Exception {
        if(null == connection) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST_NAME);
            factory.setPort(HOST_PORT);
            factory.setUsername("admin");
            factory.setPassword("admin");
            factory.setVirtualHost("/");
            connection = factory.newConnection();
        }
        return connection;
    }
    
}

```

### 1、Work Queues 工作序列

Producer消息发送给queue，多个Consumer同时往队列上消费消息

**案例**

**生产者**

```java
package com.xuenai.rabbitmq.direct;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.util.HashMap;

public class Sender {

    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
//        声明队列
//        队列名,durable是否实例化,exclusive:是否被独占,autoDelete:是否自动删除,arguments:参数
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        
        String message = "Hello World!";

//        创建一个 AMQP.BasicProperties 的构建器(Builder)，用于设置消息的各种属性
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties().builder();
//        设置消息的投递模式(delivery mode)
//        PERSISTENT_TEXT_PLAIN.getDeliveryMode() 返回的是 2，表示持久化消息，默认为 1，存储在内存中
        builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
//        设置消息的优先级(priority)
//        PERSISTENT_TEXT_PLAIN.getPriority() 返回的是 0（默认优先级）
        builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
        
        builder.messageId("" + channel.getNextPublishSeqNo());
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("order","123456");
        builder.headers(headers);
        
        channel.basicPublish("",QUEUE_NAME,builder.build(),message.getBytes("UTF-8"));

        System.out.println("Send " + message);
        
        channel.close();
        connection.close();
    }
    
}

```

**消费者**

```java
package com.xuenai.rabbitmq.direct;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;

public class PushReceiver {

    private static final String QUEUE_NAME = "hello";
    /**
     * 保持长连接，等待服务器推送的消费方式。
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);

//        进行消费
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("========================");
                String routingKey = envelope.getRoutingKey();
                System.out.println("routingKey >"+routingKey);
                String contentType = properties.getContentType();
                System.out.println("contentType >"+contentType);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println("deliveryTag >"+deliveryTag);
                System.out.println("content:"+new String(body,"UTF-8"));
                System.out.println("messageId:"+properties.getMessageId());
                properties.getHeaders().forEach((key,value)-> System.out.println("key: "+key +"; value: "+value));

                //消息处理完后，进行答复。答复过的消息，服务器就不会再次转发。
                //没有答复过的消息，服务器会一直不停转发。
                channel.basicAck(deliveryTag, false);
            }
        };
        
        channel.basicConsume(QUEUE_NAME,false,consumer);
    }
    
}

```

这个模式应该是最常用的模式

- 首先，Consumer端的autoAck字段设置的是false,这表示consumer在接收到消息后不会自动反馈服务器已消费了message，而要改在对message处理完成了之后，再调用channel.basicAck来通知服务器已经消费了该message.这样即使Consumer在执行message过程中出问题了，也不会造成message被忽略，因为没有ack的message会被服务器重新进行投递。但是，这其中也要注意一个很常见的BUG，就是如果所有的consumer都忘记调用basicAck()了，就会造成message被不停的分发，也就造成不断的消耗系统资源。这也就是 **Poison Message(毒消息)**
- 其次，message的持久性。关键的message不能因为服务出现问题而被忽略。还要注意，所有的queue是不能被多次定义的。如果一个queue在开始时被声明为durable，那在后面再次声明这个queue时，即使声明为 not durable，那这个queue的结果也还是durable的
- 然后，是中间件最为关键的分发方式。这里，RabbitMQ默认是采用的fair dispatch，也叫round-robin模式，就是把消息轮询，在所有consumer中轮流发送。这种方式，没有考虑消息处理的复杂度以及consumer的处理能力。而他们改进后的方案，是consumer可以向服务器声明一个**prefetchCount**，我把他叫做预处理能力值。**channel.basicQos(prefetchCount)**;表示当前这个consumer可以同时处理几个message。这样服务器在进行消息发送前，会检查这个consumer当前正在处理中的message(message已经发送，但是未收到consumer的basicAck)有几个，如果超过了这个consumer节点的能力值，就不再往这个consumer发布

可以将消费者拷贝一份，进行测试轮询效果，第一次生产者发送时，第一个消费者接收到，但是第二个不会接收到，这个时候再去发送一条，就会发现，第二个消费者接受到，反而第一个消费者没有



### 2、**Publish/Subscribe** 订阅发布机制

这个机制是对上面的一种补充。也就是把preducer与Consumer进行进一步的解耦。producer只负责发送消息，至于消息进入哪个queue，由exchange来分配

![订阅与发布](.\images\订阅与发布.png)

如上图，就是把producer发送的消息，交由exchange同时发送到两个queue里，然后由不同的Consumer去进行消费

**案例**

**生产者**

```java
package com.xuenai.rabbitmq.pubsub;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xuenai.rabbitmq.RabbitMQUtil;

public class EmitLogFanout {

    private static final String EXCHANGE_NAME = "fanoutExchange";

    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String message = "测试信息 INFO";
        channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes());
        
        channel.close();
        connection.close();
    }
    
}

```

**消费者**

```java
package com.xuenai.rabbitmq.pubsub;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;

public class ReceiveLogsFanout {

    private static final String EXCHANGE_NAME = "fanoutExchange";

    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String queueName = channel.queueDeclare().getQueue();
        System.out.println("队列名称为: " + queueName);
        channel.queueBind(queueName,EXCHANGE_NAME,"");

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                System.out.println("routingKey >"+routingKey);
                String contentType = properties.getContentType();
                System.out.println("contentType >"+contentType);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println("deliveryTag >"+deliveryTag);
                System.out.println("content:"+new String(body,"UTF-8"));
            }
        };
        
        channel.basicConsume(queueName,true,consumer);

    }
    
}

```

关键处就是 **type为 ”fanout” **  的exchange,这种类型的exchange只负责往所有已绑定的队列上发送消息



### 3、Routing 基于内容的路由

![基于内容的路由](.\images\基于内容的路由.png)

在 exchange 往所有队列发送消息的基础上，增加一个路由配置，指定exchange如何将不同类别的消息分发到不同的queue上

**案例**

**生产者**

```java
package com.xuenai.rabbitmq.pubsub;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xuenai.rabbitmq.RabbitMQUtil;

public class EmitLogDirect {

    private static final String EXCHANGE_NAME = "directExchange";
    /**
     * exchange有四种类型， fanout topic headers direct
     * direct类型的exchange会根据routingkey，将消息转发到该exchange上绑定了该routingkey的所有queue
     * @param args
     * @throws Exception
     */
    
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String message = "测试带 RoutingKey 的消息";
        
//        发送消息并指定 Key
        channel.basicPublish(EXCHANGE_NAME,"info",null,message.getBytes());
        channel.basicPublish(EXCHANGE_NAME,"warn",null,message.getBytes());
        channel.basicPublish(EXCHANGE_NAME,"debug",null,message.getBytes());
        
        channel.close();
        connection.close();
    }
    
    
}

```

**消费者**

```java
package com.xuenai.rabbitmq.pubsub;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;

public class ReceiveLogsDirect {

    private static final String EXCHANGE_NAME = "directExchange";

    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queueName="direct_queue";
        channel.queueDeclare(queueName,false,false,false,null);

        channel.queueBind(queueName, EXCHANGE_NAME, "info");
        channel.queueBind(queueName, EXCHANGE_NAME, "debug");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                System.out.println("routingKey >" + routingKey);
                String contentType = properties.getContentType();
                System.out.println("contentType >" + contentType);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println("deliveryTag >" + deliveryTag);
                System.out.println("content:" + new String(body, "UTF-8"));
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}

```

可以再拷贝一个消费者，然后将 `Routing Key` ，修改为 `warn` ，就会发现也接收到了



### 4、Topic 基于话题的路由

![基于话题的路由](.\images\基于话题的路由.png)

这个模式也就在上一个模式的基础上，对routingKey进行了模糊匹配单词之间用 .（点）隔开

**\* 代表一个具体的单词**（是指使用点进行分割）

**\# 代表0个或多个单词**

**案例**

**生产者**

```java
package com.xuenai.rabbitmq.pubsub;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xuenai.rabbitmq.RabbitMQUtil;

public class EmitLogTopic {
    
    private static final String EXCHANGE_NAME = "topicExchange";
    /**
     * exchange有四种类型， fanout topic headers direct
     * topic类型的exchange在根据routingkey转发消息时，可以对rouytingkey做一定的规则，比如anonymous.info可以被*.info匹配到。
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String message  = "使用 Topic 来进行发送";
        channel.basicPublish(EXCHANGE_NAME,"k.info",null,message.getBytes());
        channel.basicPublish(EXCHANGE_NAME,"kk.warn.xiaocai",null,message.getBytes());
        
        channel.close();
        connection.close();
    }

}

```

**消费者**

```java
package com.xuenai.rabbitmq.pubsub;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;

public class ReceiveLogsTopic {

    private static final String EXCHANGE_NAME = "topicExchange";

    public static void main(String[] args) throws Exception{
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName,EXCHANGE_NAME,"*.info");
        channel.queueBind(queueName,EXCHANGE_NAME,"#.xiaocai");

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                System.out.println("routingKey >"+routingKey);
                String contentType = properties.getContentType();
                System.out.println("contentType >"+contentType);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println("deliveryTag >"+deliveryTag);
                System.out.println("content:"+new String(body,"UTF-8"));
            }
        };

        channel.basicConsume(queueName,true,consumer);
        
    }
    
}

```



### 5、Publisher Confirms 发送者消息确认

RabbitMQ的消息可靠性是非常高的，但是他以往的机制都是保证消息发送到了MQ之后，可以推送到消费者消费，不会丢失消息。但是发送者发送消息是否成功是没有保证的。发送者发送消息的基础API：`Producer.basicPublish` 方法是没有返回值的，也就是说，一次发送消息是否成功，应用是不知道的，这在业务上就容易造成消息丢失。而这个模块就是通过给发送者提供一些确认机制，来保证这个消息发送的过程是成功的

发送者确认模式默认是不开启的，所以如果需要开启发送者确认模式，需要手动在channel中进行声明

```
channel.confirmSelect();
```

**发布单条信息**

即发布一条消息就确认一条消息

```java
    static void publishMessagesIndividually() throws Exception {
        try (Connection connection = RabbitMQUtil.getConnection()) {
            Channel ch = connection.createChannel();

            String queue = UUID.randomUUID().toString();
            ch.queueDeclare(queue, false, false, true, null);

            ch.confirmSelect();
            long start = System.nanoTime();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String body = String.valueOf(i);
                ch.basicPublish("", queue, null, body.getBytes());
                ch.waitForConfirmsOrDie(5_000);
            }
            long end = System.nanoTime();
            System.out.format("Published %,d messages individually in %,d ms%n", MESSAGE_COUNT, Duration.ofNanos(end - start).toMillis());
        }
    }
```

`channel.waitForConfirmsOrDie(5_000);` 这个方法就会在channel端等待RabbitMQ给出一个响应，用来表明这个消息已经正确发送到了RabbitMQ服务端。但是要注意，这个方法会同步阻塞channel，在等待确认期间，channel将不能再继续发送消息，也就是说会明显降低集群的发送速度即吞吐量



**发送批量信息**

之前单条确认的机制会对系统的吞吐量造成很大的影响，所以稍微中和一点的方式就是发送一批消息后，再一起确认

```java
    static void publishMessagesInBatch() throws Exception {
        try (Connection connection = RabbitMQUtil.getConnection()) {
            Channel ch = connection.createChannel();

            String queue = UUID.randomUUID().toString();
            ch.queueDeclare(queue, false, false, true, null);

            ch.confirmSelect();

            int batchSize = 100;
            int outstandingMessageCount = 0;

            long start = System.nanoTime();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String body = String.valueOf(i);
                ch.basicPublish("", queue, null, body.getBytes());
                outstandingMessageCount++;

                if (outstandingMessageCount == batchSize) {
                    ch.waitForConfirmsOrDie(5_000);
                    outstandingMessageCount = 0;
                }
            }

            if (outstandingMessageCount > 0) {
                ch.waitForConfirmsOrDie(5_000);
            }
            long end = System.nanoTime();
            System.out.format("Published %,d messages in batch in %,d ms%n", MESSAGE_COUNT, Duration.ofNanos(end - start).toMillis());
        }
    }
```

这种方式可以稍微缓解下发送者确认模式对吞吐量的影响。但是也有个固有的问题就是，当确认出现异常时，发送者只能知道是这一批消息出问题了， 而无法确认具体是哪一条消息出了问题。所以接下来就需要增加一个机制能够具体对每一条发送出错的消息进行处理



**异步确认**

实现的方式也比较简单，Producer在channel中注册监听器来对消息进行确认。核心代码就是一个

```
channel.addConfirmListener(ConfirmCallback var1, ConfirmCallback var2);
```

按说监听只要注册一个就可以了，那为什么这里要注册两个呢？成功一个，失败一个

然后关于这个ConfirmCallback，这是个监听器接口，里面只有一个方法： void handle(longsequenceNumber, boolean multiple) throws IOException; 这方法中的两个参数

- sequenceNumer：这个是一个唯一的序列号，代表一个唯一的消息。在RabbitMQ中，他的消息体只是一个二进制数组，默认消息是没有序列号的。那么在回调的时候，Producer怎么知道是哪一条消息成功或者失败呢？RabbitMQ提供了一个方法 int sequenceNumber =channel.getNextPublishSeqNo(); 来生成一个全局递增的序列号，这个序列号将会分配给新发送的那一条消息。然后应用程序需要自己来将这个序列号与消息对应起来。 没错！是的！需要客户端自己去做对应！
- multiple：这个是一个Boolean型的参数。如果是false，就表示这一次只确认了当前一条消息。如果是true，就表示RabbitMQ这一次确认了一批消息，在sequenceNumber之前的所有消息都已经确认完成了。

```java
    static void handlePublishConfirmsAsynchronously() throws Exception {
        try (Connection connection = RabbitMQUtil.getConnection()) {
            Channel ch = connection.createChannel();

            String queue = UUID.randomUUID().toString();
            ch.queueDeclare(queue, false, false, true, null);

            ch.confirmSelect();

            ConcurrentNavigableMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();

            ConfirmCallback cleanOutstandingConfirms = (sequenceNumber, multiple) -> {
                if (multiple) {
                    ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(
                            sequenceNumber, true
                    );
                    confirmed.clear();
                } else {
                    outstandingConfirms.remove(sequenceNumber);
                }
            };

            ch.addConfirmListener(cleanOutstandingConfirms, (sequenceNumber, multiple) -> {
                String body = outstandingConfirms.get(sequenceNumber);
                System.err.format(
                        "Message with body %s has been nack-ed. Sequence number: %d, multiple: %b%n",
                        body, sequenceNumber, multiple
                );
                cleanOutstandingConfirms.handle(sequenceNumber, multiple);
            });

            long start = System.nanoTime();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String body = String.valueOf(i);
                outstandingConfirms.put(ch.getNextPublishSeqNo(), body);
                ch.basicPublish("", queue, null, body.getBytes());
            }

            if (!waitUntil(Duration.ofSeconds(60), () -> outstandingConfirms.isEmpty())) {
                throw new IllegalStateException("All messages could not be confirmed in 60 seconds");
            }

            long end = System.nanoTime();
            System.out.format("Published %,d messages and handled confirms asynchronously in %,d ms%n", MESSAGE_COUNT, Duration.ofNanos(end - start).toMillis());
        }
    }

    static boolean waitUntil(Duration timeout, BooleanSupplier condition) throws InterruptedException {
        int waited = 0;
        while (!condition.getAsBoolean() && waited < timeout.toMillis()) {
            Thread.sleep(100L);
            waited = +100;
        }
        return condition.getAsBoolean();
    }
```



### 6、Headers 头部路由机制

direct,fanout,topic 等这些Exchange，都是以routingkey为关键字来进行消息路由的，但是这些Exchange有一个普遍的局限就是都是只支持一个字符串的形式，而不支持其他形式。Headers类型的Exchange就是一种忽略routingKey的路由方式。他通过Headers来进行消息路由。这个headers是一个键值对，发送者可以在发送的时候定义一些键值对，接受者也可以在绑定时定义自己的键值对。当键值对匹配时，对应的消费者就能接收到消息。

匹配的方式有两种

一种是all，表示需要所有的键值对都满足才行

另一种是any，表示只要满足其中一个键值就可以了

**案例**

**生产者**

```java
package com.xuenai.rabbitmq.pubsub;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.util.HashMap;
import java.util.Map;

public class EmitLogHeader {

    private static final String EXCHANGE_NAME = "logs";
    /**
     * exchange有四种类型， fanout topic headers direct
     * headers用得比较少，他是根据头信息来判断转发路由规则。头信息可以理解为一个Map
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // header模式不需要routingKey来转发，他是根据header里的信息来转发的。比如消费者可以只订阅logLevel=info的消息。
        // 然而，消息发送的API还是需要一个routingKey。 
        // 如果使用header模式来转发消息，routingKey可以用来存放其他的业务消息，客户端接收时依然能接收到这个routingKey消息。
        String routingKey = "ourTestRoutingKey";
        // The map for the headers.
        Map<String, Object> headers = new HashMap<>();
        headers.put("log_level", "warn");
        headers.put("bus_level", "product");
        headers.put("sys_level", "admin");
        
        String message = "测试头部路由信息发送";
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.HEADERS);
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties().builder();
        builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
        builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
        builder.headers(headers);
        
        channel.basicPublish(EXCHANGE_NAME,routingKey,builder.build(),message.getBytes());
        
        channel.close();
        connection.close();
    }
    
}

```

**消费者**

```java
package com.xuenai.rabbitmq.pubsub;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReceiveLogsHeader {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {
        String routingKey= "ourTestRoutingKey";

        Map<String, Object> headers = new HashMap<String, Object>();

//        特定参数吗,all 表示全部匹配成功 any 只要匹配成功一次即可
        headers.put("x-match","any");
        
        headers.put("log_level", "info");
        headers.put("bus_level", "product");
        headers.put("sys_level", "admin");

        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.HEADERS);
        String queueName = channel.queueDeclare("ourTestRoutingKey", true, false, false, null).getQueue();
        
        channel.queueBind(queueName,EXCHANGE_NAME,routingKey,headers);

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                System.out.println("routingKey >" + routingKey);
                String contentType = properties.getContentType();
                System.out.println("contentType >" + contentType);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println("deliveryTag >" + deliveryTag);
                Map<String, Object> headerInfo = properties.getHeaders();
                headerInfo.forEach((key, value) -> System.out.println("header key: " + key + "; value: " + value));
                System.out.println("content:" + new String(body, "UTF-8"));

                channel.basicAck(deliveryTag, false);
            }
        };
        
        channel.basicConsume(queueName,true,consumer);
    }
    
}

```



## 五、基础队列

### 1、经典队列

这是RabbitMQ最为经典的队列类型，在单机环境中，拥有比较高的消息可靠性。

![经典队列](.\images\经典队列.png)

**关键参数**

- 是否持久化(**Durability**)：Durability有两个选项，Durable和Transient。 Durable表示队列会将消息保存到硬盘，这样消息

  的安全性更高。但是同时，由于需要有更多的IO操作，所以生产和消费消息的性能，相比Transient会比较

  低。

- 是否自动删除(**Auto delete**)：Auto delete属性如果选择为是，那队列将在至少一个消费者已经连接，然后所有的消费者都断开连接后删除自己



### 2、仲裁队列

仲裁队列，是RabbitMQ从3.8.0版本，引入的一个新的队列类型，整个3.8.X版本，也都是在围绕仲裁队列进行完善和优化。仲裁队列相比Classic经典队列，在分布式环境下对消息的可靠性保障更高。

![仲裁队列](.\images\仲裁队列.png)

Quorum是基于Raft一致性协议实现的一种新型的分布式消息队列，他实现了持久化，多备份的FIFO队列，主要就是针对RabbitMQ的镜像模式设计的。简单理解就是quorum队列中的消息需要有集群中多半节点同意确认后，才会写入到队列中。这种队列类似于RocketMQ当中的DLedger集群。这种方式可以保证消息在集群内部不会丢失。同时，Quorum是以牺牲很多高级队列特性为代价，来进一步保证消息在分布式环境下的高可靠

对于经典队列来说，其中有个特例就是**Poison Message handling**(处理有毒的消息)。所谓毒消息是指消息一直不能被消费者正常消费(可能是由于消费者失败或者消费逻辑有问题等)，就会导致消息不断的重新入队，这样这些消息就成为了毒消息。这些读消息应该有保障机制进行标记并及时删除。Quorum队列会持续跟踪消息的失败投递尝试次数，并记录在"x-delivery-count"这样一个头部参数中。然后，就可以通过设置 Delivery limit参数来定制一个毒消息的删除策略。当消息的重复投递次数超过了Delivery limit参数阈值时，RabbitMQ就会删除这些毒消息。当然，如果配置了死信队列的话，就会进入对应的死信队列



### 3、流式队列

Stream队列是RabbitMQ自3.9.0版本开始引入的一种新的数据队列类型。这种队列类型的消息是持久化到磁盘并且具备分布式备份的，更适合于消费者多，读消息非常频繁的场景

![流式队列](.\images\流式队列.png)

Stream队列的核心是以**append-only**只添加的日志来记录消息，整体来说，就是消息将以append-only的方式持久化到日志文件中，然后通过调整每个消费者的消费进度offset，来实现消息的多次分发。下方有几个属性也都是来定义日志文件的大小以及保存时间。

这种队列提供了RabbitMQ已有的其他队列类型不太好实现的四个特点：

- large fan-outs 大规模分发

  当想要向多个订阅者发送相同的消息时，以往的队列类型必须为每个消费者绑定一个专用的队列。如果消费者的数量很大，这就会导致性能低下。而Stream队列允许任意数量的消费者使用同一个队列的消息，从而消除绑定多个队列的需求。

- Replay/Time-travelling 消息回溯

  RabbitMQ已有的这些队列类型，在消费者处理完消息后，消息都会从队列中删除，因此，无法重新读取已经消费过的消息。而Stream队列允许用户在日志的任何一个连接点开始重新读取数据。

- Throughput Performance 高吞吐性能

  Strem队列的设计以性能为主要目标，对消息传递吞吐量的提升非常明显。

- Large logs 大日志

  RabbitMQ一直以来有一个让人诟病的地方，就是当队列中积累的消息过多时，性能下降会非常明显。但是Stream队列的设计目标就是以最小的内存开销高效地存储大量的数据。使用Stream队列可以比较轻松在队列中积累百万级别的消息。



### 4、如何使用

#### 仲裁队列

Quorum队列与Classic队列的使用方式是差不多的。最主要的差别就是在声明队列时有点不同。如果要声明一个Quorum队列，则只需要在后面的arguments中传入一个参数，**x-queue-type**，参数值设定为**quorum**。

**生产者**

```java
package com.xuenai.rabbitmq.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.util.HashMap;
import java.util.Map;

public class EmitQuorum {

    private static final String QUEUE_NAME = "hello_quorum";
    
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明仲裁队列
        Map<String, Object> params = new HashMap<>();
        params.put("x-queue-type","quorum");
        channel.queueDeclare(QUEUE_NAME,true,false,false,params);
        
        String message = "Hello Quorum World!";
        
        channel.basicPublish("",QUEUE_NAME,null,message.getBytes("UTF-8"));
        System.out.println("Send " + message);
        channel.close();
        connection.close();
    }
}

```

**消费者**

```java
package com.xuenai.rabbitmq.test;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReceiveQuorum {

    private static final String QUEUE_NAME = "hello_quorum";
    
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        Map<String, Object> params = new HashMap<>();
        params.put("x-queue-type","quorum");
        channel.queueDeclare(QUEUE_NAME,true,false,false,params);
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("========================");
                String routingKey = envelope.getRoutingKey();
                System.out.println("routingKey >"+routingKey);
                String contentType = properties.getContentType();
                System.out.println("contentType >"+contentType);
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println("deliveryTag >"+deliveryTag);
                System.out.println("content:"+new String(body,"UTF-8"));
                System.out.println("messageId:"+properties.getMessageId());
                properties.getHeaders().forEach((key,value)-> System.out.println("key: "+key +"; value: "+value));
                channel.basicAck(deliveryTag, false);
            }
        };
        
        channel.basicConsume(QUEUE_NAME,true,consumer);
    }
}

```

#### 流式队列

Stream队列相比于Classic队列，在使用上就要稍微复杂一点。如果要声明一个Stream队列，则 **x-queue-type**参数要设置为 **stream**

**注意**：使用流式队列之前，必须要开启流式队列插件

```bash
# 查看所有插件
rabbitmq-plugins list

# 使用插件
rabbitmq-plugins enable rabbitmq-stream

# 重启
rabbitmq-server restart
```

**消费者**

```java
package com.xuenai.rabbitmq.test;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReceiveStream {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明流式队列
        Map<String, Object> queueArgs = new HashMap<>();
        queueArgs.put("x-queue-type", "stream");  // 必须指定为 stream 类型
        queueArgs.put("x-max-length-bytes",20_000_000_000L);
        queueArgs.put("x-stream-max-segment-size-bytes",100_000_000);

        channel.queueDeclare("stream_queue", true,   // 持久化
                false,  // 不排他
                false,  // 不自动删除
                queueArgs);


        channel.basicQos(2);
        
        Map<String, Object> consumeArgs = new HashMap<>();
        consumeArgs.put("x-stream-offset", "last");

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("========================");
                System.out.println("Received: " + new String(body, "UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };


        channel.basicConsume("stream_queue", false, consumeArgs, consumer);

        System.out.println("等待消息...");
    }
}
```

**生产者**

```java
package com.xuenai.rabbitmq.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.util.HashMap;
import java.util.Map;

public class EmitStream {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();

//        声明一个流式队列
        Map<String, Object> params = new HashMap<>();
        params.put("x-queue-type","stream");
        params.put("x-max-length-bytes",20_000_000_000L);
        params.put("x-stream-max-segment-size-bytes",100_000_000);
        channel.queueDeclare("stream_queue",true,false,false,params);
        
        String message = "Hello Stream World!";

        channel.basicPublish("","stream_queue",null,message.getBytes("UTF-8"));
        channel.close();
        connection.close();
    }
}

```

与Quorum队列类似， Stream队列的durable参数必须声明为true，exclusive参数必须声明为false，这其中，**x-max-length-bytes** 表示日志文件的最大字节数。**x-stream-max-segment-size-bytes** 每一个日志文件的最大大小。这两个是可选参数，通常为了防止stream日志无限制累计，都会配合stream队列一起声明。

然后，当要消费Stream队列时，要重点注意他的三个必要的步骤：

- channel必须设置basicQos属性。 与Spring框架集成使用时，channel对象可以在@RabbitListener声明的消费者方法中直接引用，Spring框架会进行注入。
- 正确声明Stream队列。 在Queue对象中传入声明Stream队列所需要的参数。
- 消费时需要指定offset。 与Spring框架集成时，可以通过注入Channel对象，使用原生API传入offset属性。

x-stream-offset的可选参数：

- first: 从日志队列中第一个可消费的消息开始消费
- last: 消费消息日志中最后一个消息
- next: 相当于不指定offset，消费不到消息。
- Offset: 一个数字型的偏移量
- Timestamp:一个代表时间的Data类型变量，表示从这个时间点开始消费。例如 一个小时前 Date timestamp = new Date(System.currentTimeMillis() - 60 * 60 * 1_000)



## 六、死信队列

死信队列是RabbitMQ中非常重要的一个特性。简单理解，他是RabbitMQ对于未能正常消费的消息进行的一种补救机制。死信队列也是一个普通的队列，同样可以在队列上声明消费者，继续对消息进行消费处理。

**主要参数**

- x-dead-letter-exchange：对应死信交换机 例：mirror.dlExchange
- x-dead-letter-routing-key：死信交换机对应的 routing-key
- x-message-ttl：消息过期时间
- durable：持久化

在这里，x-dead-letter-exchange指定一个交换机作为死信交换机，然后x-dead-letter-routing-key指定交换机的RoutingKey。而接下来，死信交换机就可以像普通交换机一样，通过RoutingKey将消息转发到对应的死信队列中



### 1、什么时候产生死信

有三种情况下，RabbitMQ 会将一个正常消息转换为死信

- 消息被消费者确认拒绝：消费者把  requeue 参数设置为true(false)，并且在消费后，向RabbitMQ返回拒绝。channel.basicReject或者channel.basicNack
- 消息达到预设的TTL时限还一直没有被消费
- 消息由于队列已经达到最长长度限制而被丢掉

**TTL**

TTL即最长存活时间 Time-To-Live 。消息在队列中保存时间超过这个TTL，即会被认为死亡。死亡的消息会被丢入死信队列，如果没有配置死信队列的话，RabbitMQ会保证死了的消息不会再次被投递，并且在未来版本中，会主动删除掉这些死掉的消息。

策略配置方式 - Web管理平台配置 或者 使用指令配置 60000为毫秒单位

```
rabbitmqctl set_policy TTL ".*" '{"message-ttl":60000}' --apply-to queues
```

在声明队列时指定 - 同样可以在Web管理平台配置，也可以在代码中配置

```java
Map<String, Object> args = new HashMap<String, Object>();
args.put("x-message-ttl", 60000);
channel.queueDeclare("test_queue", false, false, false, args);
```



### 2、配置死信队列

RabbitMQ中有两种方式可以声明死信队列，一种是针对某个单独队列指定对应的死信队列。另一种就是以策略的方式进行批量死信队列的配置。针对多个队列，可以使用策略方式，配置统一的死信队列。

**生成者**

```java
package com.xuenai.rabbitmq.dead;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.util.HashMap;
import java.util.Map;

public class DeadLetterProducer {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();

        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", "dead_letter_exchange");
        channel.queueDeclare("dead_letter_queue", true, false, false, params);
        String message = "Hello Dead Letter World!";
        channel.basicPublish("", "dead_letter_queue", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));

        channel.close();
        connection.close();
    }
}

```

**普通消费者**

```java
package com.xuenai.rabbitmq.dead;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeadLetterNackConsumer {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();

        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", "dead_letter_exchange");
        channel.queueDeclare("dead_letter_queue", true, false, false, params);

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("Received: " + new String(body));
//                拒绝且设置不能重新回队,进入死信队列
                channel.basicNack(envelope.getDeliveryTag(), false, false);
            }
        };

        channel.basicConsume("dead_letter_queue", false, consumer);
    }
}

```

**死信消费者**

```java
package com.xuenai.rabbitmq.dead;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeadLetterConsumer {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();

//      声明死信交换机
        channel.exchangeDeclare("dead_letter_exchange", BuiltinExchangeType.FANOUT, true, false, null);

//      声明队列时带上死信交换机参数
        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", "dead_letter_exchange");

        channel.queueDeclare("dead_letter_queue", true, false, false, params);

//      绑定队列和交换机
        channel.queueBind("dead_letter_queue", "dead_letter_exchange", "");

        // 消费消息
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Map<String, Object> headers = properties.getHeaders();
                if (headers != null) {
                    for (String key : headers.keySet()) {
                        System.out.println(key + ": " + headers.get(key));
                    }
                }
                System.out.println("死信队列获取到的信息: " + new String(body, "UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicConsume("dead_letter_queue", false, consumer);
    }
}
```



### 3、关键参数

死信在转移到死信队列时，他的Routing key 也会保存下来。但是如果配置了x-dead-letter-routing-key这个参数的话，routingkey就会被替换为配置的这个值。

另外，死信在转移到死信队列的过程中，是没有经过消息发送者确认的，**所以并不能保证消息的安全性**



### 4、如何确定是否是死信

消息被作为死信转移到死信队列后，会在Header当中增加一些消息。在官网的详细介绍中，可以看到很多内容，比如时间、原因(rejected,expired,maxlen)、队列等。然后header中还会加上第一次成为死信的三个属性，并且这三个属性在以后的传递过程中都不会更改。

- x-first-death-reason

- x-first-death-queue

- x-first-death-exchange



### 5、基于死信完成延时队列

其实从前面的配置过程能够看到，所谓死信交换机或者死信队列，不过是在交换机或者队列之间建立一种死信对应关系，而死信队列可以像正常队列一样被消费。他与普通队列一样具有FIFO的特性。对死信队列的消费逻辑通常是对这些失效消息进行一些业务上的补偿

RabbitMQ中，是不存在延迟队列的功能的，而通常如果要用到延迟队列，就会采用TTL+死信队列的方式来处理。

RabbitMQ提供了一个rabbitmq_delayed_message_exchange插件，可以实现延迟队列的功能，但是并没有集成到官方的发布包当中，需要单独去下载。



## 七、懒队列

RabbitMQ从3.6.0版本开始，就引入了懒队列(Lazy Queue)的概念。懒队列会尽可能早的将消息内容保存到硬盘当中，并且只有在用户请求到时，才临时从硬盘加载到RAM内存当中。

懒队列的设计目标是为了支持非常长的队列(数百万级别)。队列可能会因为一些原因变得非常长-也就是数据堆积

- 消费者服务宕机了
- 有一个突然的消息高峰，生产者生产消息超过消费者
- 消费者消费太慢了

默认情况下，RabbitMQ接收到消息时，会保存到内存以便使用，同时把消息写到硬盘。但是，消息写入硬盘的过程中，是会阻塞队列的。RabbitMQ虽然针对写入硬盘速度做了很多算法优化，但是在长队列中，依然表现不是很理想，所以就有了懒队列的出现。

懒队列会尝试尽可能早的把消息写到硬盘中。这意味着在正常操作的大多数情况下，RAM中要保存的消息要少得多。当然，这是以增加磁盘IO为代价的。

声明方式

![懒队列](.\images\懒队列.png)

代码中声明

```java
Map<String, Object> args = new HashMap<String, Object>();
args.put("x-queue-mode", "lazy");
channel.queueDeclare("myqueue", false, false, false, args);
```

制定策略

```bash
rabbitmqctl set_policy Lazy "^lazy-queue$" '{"queue-mode":"default"}' --apply-to queues
```

要注意的是，当一个队列被声明为懒队列，那即使队列被设定为不持久化，消息依然会写入到硬盘中。如果是在集群模式中使用，这会给集群资源带来很大的负担。

最后一句话总结：**懒队列适合消息量大且长期有堆积的队列，可以减少内存使用，加快消费速度。但是这是以大量消耗集群的网络及磁盘IO为代价的**。



## 八、插件

### 1、联邦插件

在企业中有很多大型的分布式场景，在这些业务场景下，希望服务也能够同样进行分布式部署。这样即可以提高数据的安全性，也能够提升消息读取的性能。例如，某大型企业，可能在北京机房和长沙机房分别搭建RabbitMQ服务，然后希望长沙机房需要同步北京机房的消息，这样可以让长沙的消费者服务可以直接连接长沙本地的RabbitMQ，而不用费尽周折去连接北京机房的RabbitMQ服务。这时要如何进行数据同步呢？搭建一个跨度这么大的内部子网显然就不太划算。这时就可以考虑使用RabbitMQ的Federation插件，搭建联邦队列Federation。通过Federation可以搭建一个单向的数据同步通道。

**启用插件**

```bash
# 启动插件
rabbitmq-plugins enable rabbitmq_federation
rabbitmq-plugins enable rabbitmq_federation_management

# 重启服务生效
rabbitmq-server restart
```

**配置 UpStream**

Upstream表示是一个外部的服务节点，在RabbitMQ中，可以是一个交换机，也可以是一个队列。他的配置方式是由下游服务主动配置一个与上游服务的链接，然后数据就会从上游服务主动同步到下游服务中

注意：本案例，采用本地的 RabbitMQ 与 服务器上的，上游服务器和下游服务器的交换机和队列名称最好一致

![配置UpStream](.\images\配置UpStream.png)

**配置策略**

![配置策略](.\images\配置策略.png)



如果都配置成功，查看状态是一个 running 状态

![下游服务器](.\images\下游服务器.png)

**消费者**

```java
package com.xuenai.rabbitmq.federation;

import com.rabbitmq.client.*;

import java.io.IOException;

public class DownStreamConsumer {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare("federation_exchange", BuiltinExchangeType.FANOUT,true);
        channel.queueDeclare("federation_queue", true, false, false, null);
        channel.queueBind("federation_queue", "federation_exchange", "routKey");

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("Received a message: " + new String(body));
            }
        };
        
        channel.basicConsume("federation_queue", true, consumer);
    }
}

```

**生产者**

```java
package com.xuenai.rabbitmq.federation;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xuenai.rabbitmq.RabbitMQUtil;

public class UpStreamProducer {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.exchangeDeclare("federation_exchange", BuiltinExchangeType.FANOUT,true);
        String message = "Hello World!";
        channel.basicPublish("federation_exchange", "routKey", null, message.getBytes());
        
        channel.close();
        connection.close();
    }
}

```



### 2、分片存储插件

Lazy Queue懒队列机制提升了消息在RabbitMQ中堆积的能力，但是最终，消息还是需要消费者处理消化。但是如何在消费者的处理能力有限的前提下提升消费者的消费速度呢？RabbitMQ提供的Sharding插件，就提供了一种思路。

谈到Sharding，你是不是就想到了分库分表？对于数据库的分库分表，分库可以减少数据库的IO性能压力，而真正要解决单表数据太大的问题，就需要分表。对于RabbitMQ同样，针对单个队列，如何增加吞吐量呢？ 消费者并不能对消息增加消费并发度，所以，RabbitMQ的集群机制并不能增加单个队列的吞吐量。

上面的懒队列其实就是针对这个问题的一种解决方案。但是很显然，懒队列的方式属于治标不治本。真正要提升RabbitMQ单队列的吞吐量，还是要从数据也就是消息入手，只有将数据真正的分开存储才行。RabbitMQ提供的Sharding插件，就是一个可选的方案。他会真正将一个队列中的消息分散存储到不同的节点上，并提供多个节点的负载均衡策略实现对等的读与写功能。

**启动插件**

```bash
rabbitmq-plugins enable rabbitmq_sharding
```

**配置 Sharding 策略**

![配置Sharding策略](.\images\配置Sharding策略.png)

主要配置的参数就是 `shards-per-node:3`，指定分片的个数

**配置 Sharding 交换机**

![Sharding交换机](.\images\Sharding交换机.png)

注意类型必须选择 `x-modulus-hash`

**生产者**

```java
package com.xuenai.rabbitmq.sharding;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xuenai.rabbitmq.RabbitMQUtil;

public class ShardingProducer {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
//        生产者只需要往 exchange 里发消息，不需要关系具体发送到哪里
        channel.exchangeDeclare("sharding_exchange", "x-modulus-hash");
        for (int i = 0; i < 1000; i++) {
            String message = "Sharding Message " + i;
            channel.basicPublish("sharding_exchange", String.valueOf(i), null, message.getBytes());
        }
        
        channel.close();
        connection.close();
    }
}

```

生产者将 1000 条数据发送到交换机中，此时在队列中就可以看到，因为上面策略是分 3 个

![Sharding队列](.\images\Sharding队列.png)

**消费分片数据**

现在sharding_exchange交换机上的消息已经平均分配到了三个碎片队列上。这时如何去消费这些消息呢？你会发现这些碎片队列的名字并不是毫无规律的，他是有一个固定的格式的。都是固定的这种格式：**sharding: {exchangename}-{node}-{shardingindex}** 。你当然可以针对每个队列去单独声明消费者，这样当然是能够消费到消息的，但是这样，你消费到的消息就是一些零散的消息了，这不符合分片的业务场景要求。

数据分片后，还是希望能够像一个普通队列一样消费到完整的数据副本。这时，Sharding插件提供了一种伪队列的消费方式。你可以声明一个名字为 **exchangename** 的伪队列，然后像消费一个普通队列一样去消费这一系列的碎片队列

```java
package com.xuenai.rabbitmq.sharding;

import com.rabbitmq.client.*;
import com.xuenai.rabbitmq.RabbitMQUtil;

import java.io.IOException;

public class ShardingConsumer {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitMQUtil.getConnection();
        Channel channel = connection.createChannel();
        
        channel.queueDeclare("sharding_exchange", false, false, false, null);
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(new String(body));
            }
        };

        String shardingFlag_1 = channel.basicConsume("sharding_exchange", true, consumer);
        System.out.println(shardingFlag_1);
        String shardingFlag_2 = channel.basicConsume("sharding_exchange", true, consumer);
        System.out.println(shardingFlag_2);
        String shardingFlag_3 = channel.basicConsume("sharding_exchange", true, consumer);
        System.out.println(shardingFlag_3);
    }
}

```

这个伪队列并不是真正存在的，但是注意，这个伪队列的名称必须和Sharding交换机的名称一致

使用Sharding插件后，Producer发送消息时，只需要指定虚拟Exchange，并不能确定消息最终会发往哪一个分片队列。而Sharding插件在进行消息分散存储时，虽然尽量是按照轮询的方式，均匀的保存消息。但是，这并不能保证消息就一定是均匀的。

首先，这些消息在分片的过程中，是没有考虑消息顺序的，这会让RabbitMQ中原本就不是很严谨的消息顺序变得更加雪上加霜。所以，**Sharding插件适合于那些对于消息延迟要求不严格，以及对消费顺序没有任何要求的的场景**。

然后，Sharding插件消费伪队列的消息时，会从消费者最少的碎片中选择队列。这时，如果你的这些碎片队列中已经有了很多其他的消息，那么再去消费伪队列消息时，就会受到这些不均匀数据的影响。所以，**如果使用Sharding插件，这些碎片队列就尽量不要单独使用了**。



## 九、集群

单机环境搭建起来的RabbitMQ服务有一个致命的问题，那就是服务不稳定的问题。如果只是单机RabbitMQ的服务崩溃了，那还好，大不了重启下服务就是了。但是如果是服务器的磁盘出问题了，那问题就大了。因为消息都是存储在Queue里的，Queue坏了，意味着消息就丢失了。这在生产环境上肯定是无法接受的。而RabbitMQ的设计重点就是要保护消息的安全性。

实际上 RabbitMQ 考虑了两种集群模式

- 默认的普通集群模式

这种模式使用Erlang语言天生具备的集群方式搭建。这种集群模式下，集群的各个节点之间只会有相同的元数据，即队列结构，而消息不会进行冗余，只存在一个节点中。消费时，如果消费的不是存有数据的节点， RabbitMQ会临时在节点之间进行数据传输，将消息从存有数据的节点传输到消费的节点。很显然，这种集群模式的消息可靠性不是很高。因为如果其中有个节点服务宕机了，那这个节点上的数据就无法消费了，需要等到这个节点服务恢复后才能消费，而这时，消费者端已经消费过的消息就有可能给不了服务端正确应答，服务起来后，就会再次消费这些消息，造成这部分消息重复消费。 另外，如果消息没有做持久化，重启就消息就会丢失。

并且，这种集群模式也不支持高可用，即当某一个节点服务挂了后，需要手动重启服务，才能保证这一部分消息能正常消费。所以这种集群模式只适合一些对消息安全性不是很高的场景。而在使用这种模式时，消费者应该尽量的连接上每一个节点，减少消息在集群中的传输。

- 镜像集群模式

这种模式是在普通集群模式基础上的一种增强方案，这也就是RabbitMQ的官方HA高可用方案。需要在搭建了普通集群之后再补充搭建。其本质区别在于，这种模式会在镜像节点中间主动进行消息同步，而不是在客户端拉取消息时临时同步。

并且在集群内部有一个算法会选举产生master和slave，当一个master挂了后，也会自动选出一个来。从而给整个集群提供高可用能力。这种模式的消息可靠性更高，因为每个节点上都存着全量的消息。而他的弊端也是明显的，集群内部的网络带宽会被这种同步通讯大量的消耗，进而降低整个集群的性能。这种模式下，队列数量最好不要过多。



### 1、普通集群搭建

这里以三台集群服务器的方式，并在 hosts 文件配置别名，并启动 MQ 服务

- 同步集群节点中的cookie

默认会在 /var/lib/rabbitmq/目录下生成一个.erlang.cookie。 里面有一个字符串。我们要做的就是保证集群中三个节点的这个cookie字符串一致。

**注意**：这个文件的默认权限是 400，即当前用户可以读，不要修改这个配置文件的权限

- 加入集群

```bash
# 停止服务
rabbitmqctl stop_app

# 加入集群
rabbitmqctl join_cluster --ram 集群名称
# 假设我们配置的hosts中,名称为 work01
rabbitmqctl join_cluster --ram rabbit@work01

# 启动集群
rabbitmqctl start_app
```

`--ram` 表示以Ram节点加入集群。RabbitMQ的集群节点分为disk和ram。disk节点会将元数据保存到硬盘当中，而ram节点只是在内存中保存元数据。

1. 由于ram节点减少了很多与硬盘的交互，所以，ram节点的元数据使用性能会比较高。但是，同时，这也意味着元数据的安全性是不如disk节点的。
2. 这里说的元数据仅仅只包含交换机、队列等的定义，而不包含具体的消息。因此，ram节点的性能提升，仅仅体现在对元数据进行管理时，比如修改队列queue，交换机exchange，虚拟机vhosts等时，与消息的生产和消费速度无关。
3. 如果一个集群中，全部都是ram节点，那么元数据就有可能丢失。这会造成集群停止之后就启动不起来了。RabbitMQ会尽量阻止创建一个全是ram节点的集群，但是并不能彻底阻止。所以，综合考虑，官方其实并不建议使用ram节点，更推荐保证集群中节点的资源投入，使用disk节点。

- 查看集群状态

可以通过 web 界面查看，也可以通过 `rabbitmqctl cluster_status` 命令查看



### 2、镜像集群搭建

搭建镜像集群，需要在普通集群的基础上进行搭建，通常在生产环境中，为了减少RabbitMQ集群之间的数据传输，在配置镜像策略时，会针对固定的虚拟主机 virtual host来配置。

RabbitMQ中的vritual host可以类比为MySQL中的库，针对每个虚拟主机，可以配置不同的权限、策略等。并且不同虚拟主机之间的数据是相互隔离的。

- 创建 `virtual host`

  ```bash
  # 添加
  rabbitmqctl add_vhost /mirror
  
  # 配置策略
  rabbitmqctl set_policy ha-all --vhost "/mirror" "^" '{"ha-mode":"all"}'
  ```

当然这些配置，也可以直接使用 web 界面进行查看

HA mode: 可选值 all , exactly, nodes，生产上通常为了保证高可用，就配all

- \- all : 队列镜像到集群中的所有节点，当新节点加入集群时，队列也会被镜像到这个节点。
- \- exactly : 需要搭配一个数字类型的参数(ha-params)。队列镜像到集群中指定数量的节点。如果集群内节点数少于这个数字，则队列镜像到集群内的所有节点。如果集群内节点大于这个数，当一个包含镜像的节点停止服务后，新的镜像就不会去另外找节点进行镜像备份了。
- \- nodes: 需要搭配一个字符串类型的参数。将队列镜像到指定的节点上。如果指定的队列不在集群中，不会报错。当声明队列时，如果指定的所有镜像节点都不在线，那队列会被创建在发起声明的客户端节点上。

通常镜像模式的集群已经足够满足大部分的生产场景了。虽然他对系统资源消耗比较高，但是在生产环境中，系统的资源都是会做预留的，所以正常的使用是没有问题的。但是在做业务集成时，还是需要注意队列数量不宜过多，并且尽量不要让RabbitMQ产生大量的消息堆积。



### 3、镜像集群+HaProxy+Keepalived

#### Haproxy反向代理

有了镜像集群之后，客户端应用就可以访问RabbitMQ集群中任意的一个节点了。但是，不管访问哪个服务，如果这个服务崩溃了，虽然RabbitMQ集群不会丢失消息，另一个服务也可以正常使用，但是客户端还是需要主动切换访问的服务地址。

为了防止这种情况情况，可以在RabbitMQ之前部署一个Haproxy，这是一个TCP负载均衡工具。应用程序只需要访问haproxy的服务端口，Haproxy会将请求以负载均衡的方式转发到后端的RabbitMQ服务上。



#### Keepalived防止单点

Haproxy保证了RabbitMQ的服务高可用，防止RabbitMQ服务单点崩溃对应用程序的影响。但是同时又带来了Haproxy的单点崩溃问题。如果Haproxy服务崩溃了，整个应用程序就完全无法访问RabbitMQ了。为了防止Haproxy单点崩溃的问题，可以引入keepalived组件来保证Haproxy的高可用。

keepalived是一个搭建高可用服务的常见工具。 他会暴露出一个虚拟IP(VIP)，并将VIP绑定到不同的网卡上。引入keepalived后，可以将VIP先绑定在已有的Haproxy服务上，然后引入一个从Haproxy作为一个备份。 当主Haproxy服务出现异常后，keepalived可以将虚拟IP转为绑定到从Haproxy服务的网卡上，这个过程称为VIP漂移。而对于应用程序，自始至终只需要访问keepalived暴露出来的VIP，感知不到VIP漂移的过程。这样就保证了Haproxy服务的高可用性。

![集群](.\images\集群.png)

搭建参考

https://www.yuque.com/xiaochuan-5hgfq/rqiea6/xc65icrse4kkokeh#hNFu9





## 其他

### 1、SpringBoot 集成

SpringBoot官方就集成了RabbitMQ，所以RabbitMQ与SpringBoot的集成是非常简单的。不过，SpringBoot集成RabbitMQ的方式是按照Spring的一套统一的MQ模型创建的，因此SpringBoot集成插件中对于生产者、消息、消费者等重要的对象模型，与RabbitMQ原生的各个组件有对应关系，但是并不完全相同

**引入依赖**

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

**配置参数**

```properties
server.port=8080
spring.rabbitmq.host=worker
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin
spring.rabbitmq.virtual-host=/mirror

# 单词推送消息数量
spring.rabbitmq.listener.simple.prefetch=1
# 消费者的消费线程数量
spring.rabbitmq.listener.simple.concurrency=5
# 消费者的最大线程数量
spring.rabbitmq.listener.simple.max-concurrency=10

spring.rabbitmq.listener.simple.acknowledge-mode=none
```

**声明队列**

直连模式

```java
package com.xuenai.spring.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 直连模式只需要声明队列，所有消息都通过队列转发。
 */
@Configuration
public class DirectConfig {

    @Bean
    public Queue directQueue() {
        return new Queue("direct_queue");
    }
    
}

```

Fanout 模式

```java
package com.xuenai.spring.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fanout模式需要声明exchange，并绑定queue，由exchange负责转发到queue上。
 * @author roykingw 2019年7月9日
 *
 */
@Configuration
public class FanoutConfig {

    @Bean
    public Queue fanout() {
        return new Queue("fanout.queue");
    }

    @Bean
    public FanoutExchange setFanoutExchange() {
        return new FanoutExchange("fanoutExchange");
    }
    
    @Bean
    public Binding fanoutBind() {
        return BindingBuilder.bind(fanout()).to(setFanoutExchange());
    }
    
}

```

Header 模式

```java
package com.xuenai.spring.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class HeaderConfig {
    
    @Bean
    public Queue headQueueTx(){
        return new Queue("tx_type");
    }

    @Bean
    public Queue headQueueBus(){
        return new Queue("bus_type");
    }


    @Bean
    public Queue headQueueTxBus(){
        return new Queue("tx_bus_type");
    }
    
    @Bean
    public HeadersExchange setHeaderExchange() {
        return new HeadersExchange("headerExchange");
    }

    /**
     * 绑定 tx_type = 1 的队列
     * @return 绑定
     */
    @Bean
    public Binding bindHeaderTx() {
        return BindingBuilder.bind(headQueueTx()).to(setHeaderExchange()).where("tx_type").matches(1);
    }
    
    @Bean
    public Binding bindHeaderBus() {
        return BindingBuilder.bind(headQueueBus()).to(setHeaderExchange()).where("bus_type").matches(1);
    }

    @Bean
    public Binding bindHeaderTxBus() {
        HashMap<String, Object> condMap = new HashMap<>();
        condMap.put("tx_type", "1");
        condMap.put("bus_type", "1");
        return BindingBuilder.bind(headQueueTxBus()).to(setHeaderExchange()).whereAny(condMap).match();
    }
}

```

Quorum 模式

```java
package com.xuenai.spring.config;


import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author roy
 * @desc 声明一个Quorum队列
 */
@Configuration
public class QuorumConfig {

    @Bean
    public Queue quorumQueue() {
        Map<String,Object> params = new HashMap<>();
        params.put("x-queue-type","quorum");

        return new Queue("quorum_queue",true,false,false,params);
    }
    
}

```

Topic 模式

```java
package com.xuenai.spring.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TopicConfig {
    
    @Bean
    public Queue topicQueue(){
        return new Queue("topic_queue");
    }
    
    @Bean
    public TopicExchange setTopicExchange(){
        return new TopicExchange("topicExchange");  
    }
    
    @Bean
    public Binding topicBinding(){
        return BindingBuilder.bind(topicQueue()).to(setTopicExchange()).with("*.cai");  
    }
    
    
}

```

**消费者**

```java
package com.xuenai.spring.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DirectReceiver {


    //直连模式的多个消费者，会分到其中一个消费者进行消费。类似task模式
    //通过注入RabbitContainerFactory对象，来设置一些属性，相当于task里的channel.basicQos
    @RabbitListener(queues="direct_queue",containerFactory="qos_4")
    public void directReceive(Message message, Channel channel, String messageStr) {
        System.out.println("consumer1 received message : " +messageStr);
    }

    @RabbitListener(queues = "fanout.queue")
    public void fanoutReceive(String message) {
        System.out.println("consumer2 received message : " +message);
    }
}

```

**生产者**

```java
package com.xuenai.spring.producer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @GetMapping("/directSend")
    private Object directSend(String message) throws Exception {
        //设置部分请求参数
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        messageProperties.setPriority(2);
        //设置消息转换器，如json
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        //发消息
        rabbitTemplate.send("direct_queue",new Message(message.getBytes("UTF-8"),messageProperties));
        return "message send : "+message;
    }
    
}

```



### 2、如何保证信息不丢失

这是面试时最喜欢问的问题，其实这是个所有MQ的一个共性的问题，大致的解决思路也是差不多的，但是针对不同的MQ产品会有不同的解决方案。而RabbitMQ设计之处就是针对企业内部系统之间进行调用设计的，所以他的消息可靠性是比较高的。

#### 丢失场景

![服务架构](.\images\服务架构.png)

其中，1，2，4三个场景都是跨网络的，而跨网络就肯定会有丢消息的可能。然后关于3这个环节，通常MQ存盘时都会先写入操作系统的缓存page cache中，然后再由操作系统异步的将消息写入硬盘。这个中间有个时间差，就可能会造成消息丢失。如果服务挂了，缓存中还没有来得及写入硬盘的消息就会丢失。这也是任何用户态的应用程序无法避免的。对于任何MQ产品，都应该从这四个方面来考虑数据的安全性。那我们看看用RabbitMQ时要如何解决这个问题。



#### 零丢失方案

**生产者保证消息正确发送到RibbitMQ**

对于单个数据，可以使用生产者确认机制。通过多次确认的方式，保证生产者的消息能够正确的发送到RabbitMQ中。

RabbitMQ的生产者确认机制分为同步确认和异步确认。同步确认主要是通过在生产者端使用Channel.waitForConfirmsOrDie()指定一个等待确认的完成时间。异步确认机制则是通过channel.addConfirmListener(ConfirmCallback var1, ConfirmCallback var2)在生产者端注入两个回调确认函数。第一个函数是在生产者消息发送成功时调用，第二个函数则是生产者消息发送失败时调用。两个函数需要通过sequenceNumber自行完成消息的前后对应。sequenceNumber的生成方式需要通过channel的序列获取。int sequenceNumber = channel.getNextPublishSeqNo();

当前版本的RabbitMQ，可以在Producer中添加一个ReturnListener，监听那些成功发到Exchange，但是却没有路由到Queue的消息。如果不想将这些消息返回给Producer，就可以在Exchange中，也可以声明一个alternate-exchange参数，将这些无法正常路由的消息转发到指定的备份Exchange上。

如果发送批量消息，在RabbitMQ中，另外还有一种手动事务的方式，可以保证消息正确发送。手动事务机制主要有几个关键的方法： channel.txSelect() 开启事务； channel.txCommit() 提交事务；channel.txRollback() 回滚事务； 用这几个方法来进行事务管理。但是这种方式需要手动控制事务逻辑，并且手动事务会对channel产生阻塞，造成吞吐量下降



**RabbitMQ消息存盘不丢消息**

这个在RabbitMQ中比较好处理，对于Classic经典队列，直接将队列声明成为持久化队列即可。而新增的Quorum队列和Stream队列，都是明显的持久化队列，能更好的保证服务端消息不会丢失。



**RabbitMQ 主从消息同步时不丢消息**

这涉及到RabbitMQ的集群架构。首先他的普通集群模式，消息是分散存储的，不会主动进行消息同步了，是有可能丢失消息的。而镜像模式集群，数据会主动在集群各个节点当中同步，这时丢失消息的概率不会太高。另外，启用Federation联邦机制，给包含重要消息的队列建立一个远端备份，也是一个不错的选择。



**RabbitMQ 消费者不丢失消息**

RabbitMQ在消费消息时可以指定是自动应答，还是手动应答。如果是自动应答模式，消费者会在完成业务处理后自动进行应答，而如果消费者的业务逻辑抛出异常，RabbitMQ会将消息进行重试，这样是不会丢失消息的，但是有可能会造成消息一直重复消费。

将RabbitMQ的应答模式设定为手动应答可以提高消息消费的可靠性。

```java
channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
@Override
public void handleDelivery(String consumerTag, Envelope envelope,
						BasicProperties properties, byte[] body)
											throws IOException {
        long deliveryTag = envelope.getDeliveryTag();
        channel.basicAck(deliveryTag, false);
	}
});
channel.basicConsume(queueName, true, myconsumer);
```

另外这个应答模式在SpringBoot集成案例中，也可以在配置文件中通过属性spring.rabbitmq.listener.simple.acknowledge-mode 进行指定。可以设定为 AUTO 自动应答； MANUAL手动应答；NONE 不应答； 其中这个NONE不应答，就是不启动应答机制，RabbitMQ只管往消费者推送消息后，就不再重复推送消息了，相当于RocketMQ的sendoneway， 这样效率更高，但是显然会有丢消息的可能。

最后，任何用户态的应用程序都无法保证绝对的数据安全，所以，备份与恢复的方案也需要考虑到。



### 3、如何保证消息幂等？

- RabbitMQ 的自动重试机制

当消费者消费消息处理业务逻辑时，如果抛出异常，或者不向RabbitMQ返回响应，默认情况下，RabbitMQ会无限次数的重复进行消息消费。

处理幂等问题，**首先要设定**RabbitMQ的重试次数。在SpringBoot集成RabbitMQ时，可以在配置文件中指定spring.rabbitmq.listener.simple.retry开头的一系列属性，来制定重试策略。

**然后，需要在业务上处理幂等问题**，处理幂等问题的关键是要给每个消息一个唯一的标识。在SpringBoot框架集成RabbitMQ后，可以给每个消息指定一个全局唯一的MessageID，在消费者端针对MessageID做幂等性判断

```java
//发送者指定ID字段
Message finalMessage =
MessageBuilder.withBody(message.getBytes()).setMessageId(UUID.randomUUID().toString()).build();
rabbitTemplate.send(finalMessage);
//消费者获取MessageID，自己做幂等性判断
@RabbitListener(queues = "fanout_email_queue")
public void process(Message message) throws Exception {
    // 获取消息Id
    String messageId = message.getMessageProperties().getMessageId();
    ...
}
```

要注意下这里用的message要是org.springframework.amqp.core.Message

在原生API当中，也是支持MessageId的。当然，在实际工作中，最好还是能够添加一个具有业务意义的数据作为唯一键会更好，这样能更好的防止重复消费问题对业务的影响。比如，针对订单消息，那就用订单ID来做唯一键。在RabbitMQ中，消息的头部就是一个很好的携带数据的地方。

```java
// ==== 发送消息时，携带sequenceNumber和orderNo
AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
//携带消息ID
builder.messageId(""+channel.getNextPublishSeqNo());
Map<String, Object> headers = new HashMap<>();
//携带订单号
headers.put("order", "123");
builder.headers(headers);
channel.basicPublish("", QUEUE_NAME, builder.build(), message.getBytes("UTF-8"));
// ==== 接收消息时，拿到sequenceNumber
Consumer myconsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
            BasicProperties properties, byte[] body)
            throws IOException {
                //获取消息ID
                System.out.println("messageId:"+properties.getMessageId());
                //获取订单ID
                properties.getHeaders().forEach((key,value)->
                System.out.println("key: "+key +"; value: "+value));
                // (process the message components here ...)
                //消息处理完后，进行答复。答复过的消息，服务器就不会再次转发。
                //没有答复过的消息，服务器会一直不停转发。
                channel.basicAck(deliveryTag, false);
            }
};
channel.basicConsume(QUEUE_NAME, false, myconsumer);
```



### 4、如何保证消息顺序？

某些场景下，需要保证消息的消费顺序，例如一个下单过程，需要先完成扣款，然后扣减库存，然后通知快递发货，这个顺序不能乱。如果每个步骤都通过消息进行异步通知的话，这一组消息就必须保证他们的消费顺序是一致的。

在RabbitMQ当中，针对消息顺序的设计其实是比较弱的。唯一比较好的策略就是 单队列+单消息推送。即一组有序消息，只发到一个队列中，利用队列的FIFO特性保证消息在队列内顺序不会乱。但是，显然，这是以极度消耗性能作为代价的，在实际适应过程中，应该尽量避免这种场景。

然后在消费者进行消费时，保证只有一个消费者，同时指定prefetch属性为1，即每次RabbitMQ都只往客户端推送一个消息。像这样：

```properties
spring.rabbitmq.listener.simple.prefetch=1
```



### 5、数据堆积问题

RabbitMQ一直以来都有一个缺点，就是对于消息堆积问题的处理不好。当RabbitMQ中有大量消息堆积时，整体性能会严重下降。而目前新推出的Quorum队列以及Stream队列，目的就在于解决这个核心问题。但是这两种队列的稳定性和周边生态都还不够完善，目前大部分企业还是围绕Classic经典队列构建应用。因此，在使用RabbitMQ时，还是要非常注意消息堆积的问题。尽量让消息的消费速度和生产速度保持一致。

而如果确实出现了消息堆积比较严重的场景，就需要从数据流转的各个环节综合考虑，设计适合的解决方案。

**首先在消息生产者端：**

对于生产者端，最明显的方式自然是降低消息生产的速度。但是，生产者端产生消息的速度通常是跟业务息息相关的，一般情况下不太好直接优化。但是可以选择尽量多采用批量消息的方式，降低IO频率。

**然后在RabbitMQ服务端：**

从前面的分享中也能看出，RabbitMQ本身其实也在着力于提高服务端的消息堆积能力。对于消息堆积严重的队列，可以预先添加懒加载机制，或者创建Sharding分片队列，这些措施都有助于优化服务端的消息堆积能力。另外，尝试使用Stream队列，也能很好的提高服务端的消息堆积能力。

**接下来在消息消费者端：**

要提升消费速度最直接的方式，就是增加消费者数量了。尤其当消费端的服务出现问题，已经有大量消息堆积时。这时，可以尽量多的申请机器，部署消费端应用，争取在最短的时间内消费掉积压的消息。但是这种方式需要注意对其他组件的性能压力。对于单个消费者端，可以通过配置提升消费者端的吞吐量。例如

```properties
# 单次推送消息数量
spring.rabbitmq.listener.simple.prefetch=1
# 消费者的消费线程数量
spring.rabbitmq.listener.simple.concurrency=5
```

灵活配置这几个参数，能够在一定程度上调整每个消费者实例的吞吐量，减少消息堆积数量。

当确实遇到紧急状况，来不及调整消费者端时，可以紧急上线一个消费者组，专门用来将消息快速转录。保存到数据库或者Redis，然后再慢慢进行处理。
