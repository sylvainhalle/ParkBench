ParkBench: a flexible environment for running tests in batch
============================================================


To learn how to use ParkBench and look at some tutorials, please refer to
the following:

- [A 10-minute tutorial to ParkBench](http://sylvainhalle.github.io/ParkBench/instructions.html)
- [Advanced features](http://sylvainhalle.github.io/ParkBench/advanced.html)

Table of Contents                                                    {#toc}
-----------------

- [Quick start guide](#quickstart)
- [About the author](#about)

Quick start guide                                             {#quickstart}
-----------------

1. First make sure you have the following installed:

  - The Java Development Kit (JDK) to compile. ParkBench was developed and
    tested on version 7 of the JDK, but it should normally work from
    version 6 onwards.
  - [Ant](http://ant.apache.org) to automate the compilation and build
    process

2. Download the AntRun template from
   [GitHub](https://github.com/sylvainhalle/AntRun) or clone the repository
   using Git:
   
   git@github.com:sylvainhalle/AntRun.git

3. Override any defaults, and specify any dependencies your project
   requires by editing `config.xml`. In particular, you may want
   to change the name of the Main class.

4. Start writing your code in the `Source/Core` folder, and your unit
   tests in `Source/CoreTest`. Optionally, you can create an Eclipse
   workspace out of the `Source` folder, with `Core` and `CoreTest` as
   two projects within this workspace.

5. Use Ant to build your project. To compile the code, generate the
   Javadoc, run the unit tests, generate a test and code coverage report
   and bundle everything in a runnable JAR file, simply type `ant` (without
   any arguments) on the command line.
   
6. If dependencies were specified in step 4 and are not present in the
   system, type `ant download-deps`, followed by `ant install-deps` to
   automatically download and install them before compiling. The latter
   command might require to be run as administrator --the way to do this
   varies according to your operating system (see below).

Otherwise, use one of the many [tasks](#tasks) that are predefined.


About the author                                                   {#about}
----------------

ParkBench was written by [Sylvain Hallé](http://leduotang.ca/sylvain),
associate professor at [Université du Québec à
Chicoutimi](http://www.uqac.ca/), Canada.
