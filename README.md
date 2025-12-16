[![Published on Vaadin Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/json-migration-helper)
[![Stars on vaadin.com/directory](https://img.shields.io/vaadin-directory/star/json-migration-helper.svg)](https://vaadin.com/directory/component/json-migration-helper)
[![Build Status](https://jenkins.flowingcode.com/job/JsonMigrationHelper-addon/badge/icon)](https://jenkins.flowingcode.com/job/JsonMigrationHelper-addon)
[![Maven Central](https://img.shields.io/maven-central/v/com.flowingcode.vaadin/json-migration-helper)](https://mvnrepository.com/artifact/com.flowingcode.vaadin/json-migration-helper)
[![Javadoc](https://img.shields.io/badge/javadoc-00b4f0)](https://javadoc.flowingcode.com/artifact/com.flowingcode.vaadin/json-migration-helper)

# Json Migration Helper

Provides a compatibility layer for JSON handling to abstract away breaking changes introduced in Vaadin version 25.

## Features

- **Zero-effort migration**: Write your code once and run it seamlessly on Vaadin 14, 23, 24 and 25
- **Automatic version detection**: Detects the runtime Vaadin version and uses the appropriate JSON handling strategy
- **Drop-in replacement**: Simple static methods that replace version-specific APIs
- **Client Callable compatibility**: Mechanisms to handle JSON arguments and return types in `@ClientCallable` methods.
- **JsonSerializer and JsonCodec**: Includes `JsonSerializer` and `JsonCodec` classes for serialization and deserialization of elemental JSON values.


## Download release

[Available in Vaadin Directory](https://vaadin.com/directory/component/json-migration-helper)

### Maven install

Add the following dependencies in your pom.xml file:

```xml
<dependency>
   <groupId>com.flowingcode.vaadin</groupId>
   <artifactId>json-migration-helper</artifactId>
   <version>X.Y.Z</version>
</dependency>
```
<!-- the above dependency should be updated with latest released version information -->

Release versions are available from Maven Central repository. For SNAPSHOT versions see [here](https://maven.flowingcode.com/snapshots/).

## Release notes

See [here](https://github.com/FlowingCode/JsonMigrationHelper/releases)

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome. There are two primary ways you can contribute: by reporting issues or by submitting code changes through pull requests. To ensure a smooth and effective process for everyone, please follow the guidelines below for the type of contribution you are making.

#### 1. Reporting Bugs and Requesting Features

Creating an issue is a highly valuable contribution. If you've found a bug or have an idea for a new feature, this is the place to start.

* Before creating an issue, please check the existing issues to see if your topic is already being discussed.
* If not, create a new issue, choosing the right option: "Bug Report" or "Feature Request". Try to keep the scope minimal but as detailed as possible.

> **A Note on Bug Reports**
> 
> Please complete all the requested fields to the best of your ability. Each piece of information, like the environment versions and a clear description, helps us understand the context of the issue.
> 
> While all details are important, the **[minimal, reproducible example](https://stackoverflow.com/help/minimal-reproducible-example)** is the most critical part of your report. It's essential because it removes ambiguity and allows our team to observe the problem firsthand, exactly as you are experiencing it.

#### 2. Contributing Code via Pull Requests

As a first step, please refer to our [Development Conventions](https://github.com/FlowingCode/DevelopmentConventions) page to find information about Conventional Commits & Code Style requirements.

Then, follow these steps for creating a contribution:
 
- Fork this project.
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- For commit message, use [Conventional Commits](https://github.com/FlowingCode/DevelopmentConventions/blob/main/conventional-commits.md) to describe your change.
- Send a pull request for the original project.
- Comment on the original issue that you have implemented a fix for it.

## License & Author

This library is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

Json Migration Helper is written by Flowing Code S.A.

# Developer Guide

## Using Lombok @ExtensionMethod

The `JsonMigration` class is designed to be used with Lombok's `@ExtensionMethod` annotation. This allows you to call the helper methods as if they were instance methods of `Element` or `DomEvent`:

```java
import com.flowingcode.vaadin.jsonmigration.JsonMigration;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(JsonMigration.class)
public class MyComponent extends Div {
   
   public MyComponent() {
       getElement().setPropertyJson("property", jsonValue);
       
       getElement().executeJs("console.log($0)", jsonValue)
          .then(json->{ ... });
       
       getElement().addEventListener("click", event -> {
           JsonObject eventData = event.getEventData();
       });
   }
}
```

## Returning JSON from ClientCallable methods

When a `@ClientCallable` method needs to return a JSON value, use `convertToClientCallableResult` to ensure compatibility across Vaadin versions:

```java
@ClientCallable
public JsonValue getJsonData() {
    JsonValue json = ...;
    return JsonMigration.convertToClientCallableResult(json);
}
```

## Receiving JSON in ClientCallable methods

If the method receives `JsonValue` as an argument, it cannot be annotated with `ClientCallable` because of compatibility issues. `LegacyClientCallable` should be used instead.

To use `LegacyClientCallable`, you must use instrumentation. This can be done via `JsonMigration.instrumentClass` or by using `InstrumentedRoute` / `InstrumentationViewInitializer`.

**Note:** Instrumentation is a complex mechanism. While it might warrant a rewrite of the affected code, it is offered here to preserve compatibility with existing implementations.

```java
@InstrumentedRoute("legacy-view")
public class ViewWithElementalCallables extends Div {
    @LegacyClientCallable
    public void receiveJson(JsonValue json) {
        // ...
    }
}

// Register via META-INF/services/com.vaadin.flow.server.VaadinServiceInitListener
// or use `@SpringComponent` with Spring.
public class ViewInitializerImpl extends InstrumentationViewInitializer {
  @Override
  public void serviceInit(ServiceInitEvent event) {
    registerInstrumentedRoute(ViewWithElementalCallables.class);
  }
}
```

This feature requires a dependency with ASM (which is not provided out-of-the-box in Vaadin 14-23):
```
<dependency>
    <groupId>org.ow2.asm</groupId>
    <artifactId>asm</artifactId>
    <version>9.8</version>
</dependency>
```

## Direct Usage

The helper methods can also be used directly from the `JsonMigration` class:

```java
import com.flowingcode.vaadin.jsonmigration.JsonMigration;
import elemental.json.Json;
import elemental.json.JsonValue;

// ...

// 1. Setting a JSON Property
JsonValue json = Json.createObject();
// ... populate json
JsonMigration.setPropertyJson(element, "property", json);

// 2. Executing JavaScript
JsonMigration.executeJs(element, "console.log($0)", "Hello World");

// 3. Getting Event Data
element.addEventListener("click", event -> {
    JsonObject eventData = JsonMigration.getEventData(event);
    // ...
}).addEventData("event.detail");
```


## Special configuration when using Spring

By default, Vaadin Flow only includes `com/vaadin/flow/component` to be always scanned for UI components and views. For this reason, the add-on might need to be allowed in order to display correctly. 

To do so, just add `com.flowingcode` to the `vaadin.allowed-packages` property in `src/main/resources/application.properties`, like:

```
vaadin.allowed-packages = com.vaadin,org.vaadin,dev.hilla,com.flowingcode
```
 
More information on Spring scanning configuration [here](https://vaadin.com/docs/latest/integrations/spring/configuration/#configure-the-scanning-of-packages).
