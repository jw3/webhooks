WebHooks with Akka HTTP
==========================
[![Build Status](https://travis-ci.org/jw3/webhooks.svg?branch=master)](https://travis-ci.org/jw3/webhooks)

Mixins for adding web hooks.

## Subscription API

A set of endpoints are provided to
- Subscribe (PUT)
- Unsubscribe (DELETE)
- View subscriptions (GET)

The path to the endpoints are configurable, defaulting to /hook

#### Subscribe

Params
  - host: String - callback hostname or ip
  - port: Int    - callback port number
  - path: String - path of callback on host
  - body: String - message to call back with (supports interpolation)
  - span: String - time to live for this hook (not implemented)
  - method: String - callback http method

Returns
  - uuid: String - id of subscription

#### Unsubscribe

Params
  - id: String


## Response Interpolation

The subscription can specify a body that will be used in callbacks.
The body can escape values that will be interpolated from the scope of the fired event on the server side.

todo;; example

## Hook TTL (todo)

Hooks can have a time to live set, which can be a length of time, number of invocations, or a composite

```"2x" | "30s" | "2x|30s"```

## Using this library

Add a resolver to your sbt build

```resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"```

Add dependency

```"com.rxthings" %% "webhooks" % "0.4"```


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
