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

First make sure you have the following installed:

- The Java Development Kit (JDK) to compile. ParkBench was developed and
  tested on version 7 of the JDK, but it should normally work from
  version 6 onwards.
- [Ant](http://ant.apache.org) to automate the compilation and build
  process

Download the AntRun template from
[GitHub](https://github.com/sylvainhalle/AntRun) or clone the repository
using Git:

    git@github.com:sylvainhalle/AntRun.git

### Installing dependencies

The project requires the following libraries to be present in the system:

- The [Apache Commons CLI](http://commons.apache.org/proper/commons-cli/)
  to handle command-line parameters *(tested with version 1.3)*
- The [json-simple](https://code.google.com/p/json-simple/) library for
  fast JSON parsing *(tested with version 1.1.1)*
- The [Bullwinkle parser](https://github.com/sylvainhalle/Bullwinkle),
  an on-the-fly parser for BNF grammars

Using Ant, you can automatically download any libraries missing from your
system by typing:

    ant download-deps

This will put the missing JAR files in the `deps` folder in the project's
root. These libraries should then be put somewhere in the classpath, such as
in Java's extension folder (don't leave them there, it won't work). You can
do that by typing (**with administrator rights**):

    ant install-deps

or by putting them manually in the extension folder. Type `ant init` and it
will print out what that folder is for your system.

Do **not** create subfolders there (i.e. put the archive directly in that
folder).

### Compiling

Compile the sources by simply typing:

    ant

This will produce a file called `parkbench.jar` in the folder. This file is a
stand-alone library, so it can be moved around to the location of your choice.

In addition, the script generates in the `doc` folder the Javadoc
documentation for using Cornipickle. This documentation is also embedded in
the JAR file. To show documentation in Eclipse, right-click on the jar,
click "Properties", then fill the Javadoc location (which is the JAR
itself).

### Installing external software

ParkBench also requires the following (optional) software to run:

- [GnuPlot](http://gnuplot.info) for generating graphs. Under Windows, please
  make sure the executable is in `C:\Program Files\gnuplot\binary`. Under other
  systems, simply ensure that GnuPlot is in the system's path.
- [PDFtk](https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/) for bundling
  multiple graphs into a single PDF document. Install it in the default location
  and it should work for all systems.

Now what?
---------

You are ready to use ParkBench to create your own test suites. Please read
the [instructions](http://sylvainhalle.github.io/ParkBench/instructions.html)
on how to use ParkBench.


About the author                                                   {#about}
----------------

ParkBench was written by [Sylvain Hallé](http://leduotang.ca/sylvain),
associate professor at [Université du Québec à
Chicoutimi](http://www.uqac.ca/), Canada.
