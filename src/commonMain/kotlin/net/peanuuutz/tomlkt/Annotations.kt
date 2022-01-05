package net.peanuuutz.tomlkt

import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Comment(vararg val texts: String)

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Fold

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Multiline

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Literal