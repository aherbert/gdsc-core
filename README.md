GDSC ImageJ Core
================

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://travis-ci.com/aherbert/gdsc-core.svg?branch=master)](https://travis-ci.com/aherbert/gdsc-core)
[![Coverage Status](https://coveralls.io/repos/github/aherbert/gdsc-core/badge.svg?branch=master)](https://coveralls.io/github/aherbert/gdsc-core?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.ac.sussex.gdsc/gdsc-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.ac.sussex.gdsc/gdsc-core/)
[![Javadocs](https://javadoc.io/badge2/uk.ac.sussex.gdsc/gdsc-core/javadoc.svg)](https://javadoc.io/doc/uk.ac.sussex.gdsc/gdsc-core)

This package contains core code used by the GDSC ImageJ and GDSC SMLM ImageJ
plugins. It is a dependency for both of those packages.

- gdsc-core: Contains utilities for image and 2D/3D data analysis
- gdsc-core-ij: Contains extensions to ImageJ for image analysis and display

Maven Installation
------------------

This package is used by other GDSC packages. It is only necessary to perform an
install if you are building the other packages from the source code.

The code depends on the gdsc-analytics and gdsc-test artifacts so you will have
to install this to your local Maven repository before building:

1. Clone the required repositories

        git clone https://github.com/aherbert/gdsc-analytics.git
        git clone https://github.com/aherbert/gdsc-test.git
        git clone https://github.com/aherbert/gdsc-core.git

2. Build the code and install using Maven

        cd gdsc-analytics
        mvn install
        cd ..
        cd gdsc-test
        mvn install
        cd ..
        cd gdsc-core
        mvn install
        cd ..

	This will produce gdsc-[artifact]-[VERSION].jar files in the local Maven
	repository. You can now build the other GDSC packages that depend on this
	code.


Legal
-----

See [LICENSE](LICENSE.txt)


# About #

###### Owner(s) ######
Alex Herbert

###### Institution ######
[Genome Damage and Stability Centre, University of Sussex](http://www.sussex.ac.uk/gdsc/)

###### URL ######
[GDSC ImageJ plugins](http://www.sussex.ac.uk/gdsc/intranet/microscopy/UserSupport/AnalysisProtocol/imagej/gdsc_plugins/)
