WebHooks with Akka
==========================
[![Build Status](https://travis-ci.org/jw3/webhooks.svg?branch=master)](https://travis-ci.org/jw3/webhooks)

Webhooks APIs for Actors and REST base on interpolation of system events into the predefined body of an http entity.

Subscribers provide a message template and at least one event topic to subscribe to.  When the event type is received the public interface of the event is used to populate the subscription message body using the templated and interpolation.

## Actor Subscription API

## HTTP Subscription API

- Subscribe (PUT)
- Unsubscribe (DELETE)
- View subscriptions (GET)

The path to the endpoints are configurable, defaulting to /hook

#### Subscribe

Params
  - `url`: String  - uri for callback
  - `method`: String - callback http method
  - `body`: String - message to call back with (supports interpolation)
  - `topics`: List[String] - fqcn list of events to listen for

Returns
  - `uuid`: String - id of subscription

#### Unsubscribe

Params
  - `id`: String


## Response Interpolation

The subscription can specify a body that will be used in callbacks.
The body can escape values that will be interpolated from the scope of the fired event on the server side.

todo;; example

## Hook Time to Live (TTL) (todo)

Hooks can have a TTL set, which can be a length of time, number of invocations, or a composite

```"2x" | "30s" | "2x|30s"```

## Using this library

Add the resolver

```resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"```

Add the dependency

```"com.github.jw3" %% "webhooks" % "0.6"```

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/jw3/webhooks/issues).

## LICENSE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
