GDSC Core ImageJ
================

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://github.com/aherbert/gdsc-core/actions/workflows/build.yml/badge.svg)](https://github.com/aherbert/gdsc-core/actions/workflows/build.yml)
[![Coverage Status](https://coveralls.io/repos/github/aherbert/gdsc-core/badge.svg?branch=master)](https://coveralls.io/github/aherbert/gdsc-core?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/uk.ac.sussex.gdsc/gdsc-core-ij)](https://search.maven.org/artifact/uk.ac.sussex.gdsc/gdsc-core-ij/)
[![Javadocs](https://javadoc.io/badge2/uk.ac.sussex.gdsc/gdsc-core-ij/javadoc.svg)](https://javadoc.io/doc/uk.ac.sussex.gdsc/gdsc-core-ij)

This package contains core code used by the GDSC ImageJ and GDSC SMLM ImageJ
plugins. It is a dependency for both of those packages; all code within this
package requires the ImageJ classes. The package contains
many utilities created to extend functionality to analyse and display images
within ImageJ including:

- Additional generic dialogs for plugins
- Additional ROI objects for display
- ROI hit testing
- ImageProcessor extensions for specialised image display
- Look-up tables (LUTs)
- Window organiser
- Histogram plot
- Specialised TIFF encoder/decoder with in-memory seekable streams
- Logging and progress tracking extensions

