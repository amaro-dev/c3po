# C3PO application

## Introduction

The files in this folder describe the features and UI of the application so any agent can understand its goals

## Goals

This application aims to help users to manage Android devices by providing easier ways to interact with its internal feature and instrument it by using the provided command line tools from Android SDK.

## Audience 

Its main audience are developers and anyone involved in the mobile product development, although it intends to be as easy as possible so any Android entusiast can use.

## Structure

It's a desktop application made with Kotlin using MVI architecture and Compose Multiplatform. It also connects to a companion App that runs in the device and is installed by the desktop application when not available. 

This companion App helps providing every feature and information that we can't retrieve by using terminal commands.

## Documents

This folder has its own structure with many documents that describe not only the functionality but also the layout components we expect to have so we can deliver everything we expect our users to be delivered

These are the folders:

- Layout: Brings documents to describe UI elements on each screen, our Design System definition and all layout related information.
- Mockups: Images with the mockups generated so we can implment in the application.
- Features: Documentation about the features, what the user can do and how they do it.

