# TellyDatabases
[![](https://jitpack.io/v/tellymc/TellyDatabases.svg)](https://jitpack.io/#tellymc/TellyDatabases)

**Versions:**\
 Java 8: 1.0.5\
Java 17: 1.0.5-26.1.2

## How to Install

1) Add the Repository within the Pom.xml
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

2) Add the Dependency within the Pom.xml
```xml
<dependency>
    <groupId>com.github.tellymc</groupId>
    <artifactId>TellyDatabases</artifactId>
    <version>{Version right under Title}</version>
</dependency>
```

3) Reload Maven

## How to Use
1) Add TellyDatabases into the main class
```java
public final class TestDB extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        
        // Get the instance and initialize it
        TellyDatabases tellyDatabases = new TellyDatabases(this);
        tellyDatabases.init();
    }
    
    public MyDatabase getTellyDatabase() {
        return database;
    }
}
```

2) Retrieve the database manager from telly databases
```java
public final class TestDB extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {

        // Get the instance and initialize it
        TellyDatabases tellyDatabases = new TellyDatabases(this);
        tellyDatabases.init();

        // Save the database manager from telly databases
        databaseManager = tellyDatabases.getDatabaseManager();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
```

3) Create a new database and connection pool in a new class
```java
public class MyDatabase {
}
```
4) Extend it with to be a database and do the overrides
```java
public class MyDatabase extends Database {
    
    // Set the name of the connection pool that will be created
    @Override
    public String getPoolName() {
        return "MyDatabasePool";
    }

    // Set the host
    @Override
    public String getHost() {
        return "localhost";
    }

    // Set the database name
    @Override
    public String getDatabaseName() {
        return "TestDB";
    }

    // Set the username
    @Override
    public String getUsername() {
        return "root";
    }

    // Set the password
    @Override
    public String getPassword() {
        return "*********";
    }

    // Create the tables in the database as defaults
    @Override
    public String[] getTables() {
        return new String[] {"CREATE TABLE IF NOT EXISTS Money(UUID varchar(36), Cash INT)"};
    }
}
```

5) Add the new database instance into the main class
```java
public final class TestDB extends JavaPlugin {

    private DatabaseManager databaseManager;
    
    // Add the database
    private MyDatabase database;

    @Override
    public void onEnable() {

        // Get the instance and initialize it
        TellyDatabases tellyDatabases = new TellyDatabases(this);
        tellyDatabases.init();

        // Save the database manager from telly databases
        databaseManager = tellyDatabases.getDatabaseManager();
        
        // Create an instance of the database that you made
        database = new MyDatabase();
        
        // This will start the database that you just created and make the tables and connection pool
        databaseManager.initialize(database);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    // Get the database
    public MyDatabase getMyDatabase() {
        return database;
    }
}
```

## Examples
1) Example insertion into the database on the player join
```java
public class PlayerJoin implements Listener {
    
    // Get the instance of the main class
    private final TestDB plugin;

    public PlayerJoin(TestDB plugin) {
        this.plugin = plugin;
    }
    
    // Listen for player 
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        
        // Get the database manager and execute an update to the database you just created
        // Arguments go as:
        // 1 Database
        // 2 The String of SQL you are executing
        // 3+ Any values inside the line of SQL that you are changing from the ?
        plugin.getDatabaseManager().executeUpdate(

                plugin.getMyDatabase(),
                "INSERT INTO Money (UUID, Cash) VALUES (?, ?)",
                event.getPlayer().getUniqueId().toString(),
                "100"
        // After the query/update has happened asynchronously, you are able to execute any code of your wish
        ).thenAccept((success) -> {
            
            event.getPlayer().sendMessage("Success");

        });
    }
}
```

2) Editing more parts of the connection pool for the database
```java
public class MyDatabase extends Database {

    // Set the name of the connection pool that will be created
    @Override
    public String getPoolName() {
        return "MyDatabasePool";
    }
    
    // Set the host
    @Override
    public String getHost() {
        return "localhost";
    }
    
    // Set the database name
    @Override
    public String getDatabaseName() {
        return "TestDB";
    }

    // Set the username
    @Override
    public String getUsername() {
        return "root";
    }
    
    // Set the password
    @Override
    public String getPassword() {
        return "*********";
    }
    
    // // Create the tables in the database as defaults
    @Override
    public String[] getTables() {
        return new String[] {"CREATE TABLE IF NOT EXISTS Money(UUID varchar(36), Cash INT)"};
    }
    
    // Manually set the port for the database
    @Override
    public int getPort() {
        return 3306;
    }

    // Manually set the max pool size
    @Override
    public int getMaxPoolSize() {
        return 10;
    }

    // Manually set how many pools can be idle at the same time
    @Override
    public int getMaxPoolIdle() {
        return 2;
    }

    // Manually set how long it takes for a connection to timeout on the pool
    @Override
    public long getConnectionTimeout() {
        return 30000;
    }

    // Manually set how long it takes for a connection to timeout because of being idle
    @Override
    public long getIdleTimeout() {
        return 600000;
    }

    // Manually set how long a connection can last before it will be closed
    @Override
    public long getMaxLifetime() {
        return 1800000;
    }

    // Manually set if you want to use SSL
    @Override
    public boolean getUseSsl() {
        return false;
    }
}
```