/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.gradle.node.npm.task.NpmTask

plugins {
  alias(libs.plugins.node.gradle)
}

group = "com.webauthn4j"

description = "WebAuthn4J Spring Security Sample SPA angular client library"

configure<com.github.gradle.node.NodeExtension> {
  download.set(true)

  // Version of node to use.
  version.set("20.14.0")

  // Version of npm to use.
  npmVersion.set("10.8.1")
}

tasks.named<NpmTask>("npm_run_build") {
  args.set(listOf("--production"))
  group = "build"
}

tasks.named<NpmTask>("npm_run_test") {
  group = "verification"
}

tasks.named("npm_install") {
  group = "other"
}

tasks.named("npm_outdated") {
  group = "other"
}

// npm tasks start with npm_run executes corresponding npm scripts

tasks.named<NpmTask>("npm_run_watchTest") {
  group = "verification"
}

tasks.named<NpmTask>("npm_run_e2e") {
  group = "verification"
}

tasks.named<NpmTask>("npm_run_start") {
  group = "other"
}

tasks.named<NpmTask>("npm_run_ngUpdate") {
  group = "other"
}

tasks.named<NpmTask>("npm_run_ngUpdateCore") {
  group = "other"
}

tasks.named<NpmTask>("npm_run_ngUpdateCli") {
  group = "other"
}

// processResources is a Java task
val processResources by tasks.existing
val npm_run_build by tasks.existing
val classes by tasks.existing
// classes.dependsOn npm_run_build
classes.configure { dependsOn(npm_run_build) }

// npm_run_build.mustRunAfter processResources
npm_run_build.configure { mustRunAfter(processResources) }

// test is a Java task
val npm_run_test by tasks.existing
val test by tasks.existing
test.configure { dependsOn(npm_run_test) }

sonar {
  isSkipProject = true
}
