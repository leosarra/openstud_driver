# OpenStud Driver [![Build Status](https://travis-ci.org/LithiumSR/openstud_driver.svg?branch=master)](https://travis-ci.org/LithiumSR/openstud_driver)

OpenStud driver is Java Libary to obtain infos from Sapienza University's Infostud.
This library is thread-safe and Android-friendly.

## Getting started

### Prerequisites
This application is written with JDK8 in mind. If you don't have a Java Development Kit installed you can download it from [the Oracle website](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

### Compile from source
- `git clone` or download this repo.
- open a terminal in the directory where the sources are stored.
- execute `mvn install -DskipTests` . You will find the .jar file in the target folder.

### Add to your project

OpenStud Driver can be easily added to your existing project through Maven or Gradle.

**Maven** 

1) Add the JitPack repository
```
<repositories>
	<repository>
	    <id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```
2) Add the dependency
```
<dependency>
    <groupId>com.github.LithiumSR</groupId>
    <artifactId>openstud_driver</artifactId>
    <version>0.6</version>
</dependency>
```

**Gradle** 

1) Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
		maven { url 'https://jitpack.io' }
	}
}
```
2) Add the dependency
```
dependencies {
    implementation 'com.github.LithiumSR:openstud_driver:0.6'
}
```


## Documentation

Soon<sup>(tm)</sup>

### Examples
```Logger log = Logger.getLogger("lithium.openstud");
 Openstud osb = new OpenstudBuilder().setPassword("myPassword").setStudentID(123456).setLogger(log).build();
 osb.login();
 Student st=osb.getInfoStudent(); //Get personal infos about a student
 List<ExamDoable> doable=osb.getExamsDoable(); //Get a list of exams that the student hasn't passed yet
 List<ExamPassed> passed=osb.getExamsPassed()); //Get a list of exams that the student passed with flying colors :)
 List<ExamReservation> active = osb.getActiveReservations(); //Get a list of reservations that the student has already placed
 List<ExamReservation> available = osb.getAvailableReservations(doable.get(0),st); //Get a list of the reservations avaiable for a particular exam
 Pair<Integer,String> pr = osb.insertReservation(available.get(0)); //place a reservation for a particulare session of an exam
 byte[] pdf=osb.getPdf(active.get(0)); //Download the PDF of a particular active reservation
 int result = osb.deleteReservation(active.get(0)); //Delete an active reservation
 ```
 
 ## Dependencies
 - [Square OkHttp](https://github.com/square/okhttp)
 - [JUnit](https://github.com/junit-team/junit4)
 - [org/Json](https://github.com/stleary/JSON-java)
 - [Apache HttpComponents](https://hc.apache.org/)
 - [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)
 - [Apache Commons-IO](https://commons.apache.org/proper/commons-io/)