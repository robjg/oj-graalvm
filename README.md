# Oddjob GraalVM Script Plugin

Provides a plugin for Oddjob that evaluates script attribute
expressions with GraalVM JavaScript interpreter.

The big advantage of using this was to support Function expressions
for configuring things such as Consumers as properties of 
jobs and services.

The big problem is that binding support is limited compared with
Nashorn, all session objects must be copied to the Graal binding
to be available and getters aren't recognised as JavaScript 
properties.

This is experimental only and won't be actively maintained.