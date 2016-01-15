Akka WebHooks
==========================
[![Build Status](https://travis-ci.org/jw3/awebapi.svg?branch=master)](https://travis-ci.org/jw3/awebapi)

WebHook API using Akka HTTP

## Subscription API

A set of endpoints are provided to
- Subscribe (PUT)
- Unsubscribe (DELETE)
- View subscriptions (GET)

The path to the endpoints default to /hook

## Response Interpolation

The subscription can specify a body that will be used in callbacks.
The body can escape values that will be interpolated from the scope of the fired event on the server side.

todo;; example

## Using this library

Add a resolver to your sbt build

```resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"```

Add dependency

```"wiii" %% "awebapi" % "0.2"```


## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/jw3/awebapi/issues).

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
