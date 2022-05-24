Compukit UK101 Simulation
=========================
This project is a simulation of a Compukit UK101 hobby computer from the 
late 1970s/early 1980s.  The program is written entirely in Java and should 
therefore run easily on most systems. 

Full details can be found at:
   The project homepage:  http://uk101.sourceforge.net
   Latest documentation:  http://uk101.sourceforge.net/docs
   The download pages:    http://sourceforge.net/projects/uk101

The simulation is reasonably complete and includes the following
hardware elements:

 - 6502 CPU
 - RAM and ROM store
 - Keyboard input
 - VDU output
 - Cassette tape storage
  
A Java Swing GUI interface allows the simulation to be operated in a 
manner very similar to a real UK101. 

Licence Information
-------------------
All new software for this project is covered by the project's BSD
open source licence.  But please note that the system ROMs and some 
of the sample code come from original UK101 sources and I do not 
own the copyright for these.  I do not know who the current copyright
owners are and I sincerely hope no one will mind this 40-year old 
software being made available in my packages.  If any of the copyright
owners have an issue with this, please contact me and I will remove 
anything that should not be here.  

Installation and Operation 
--------------------------
The program, sample code and a copy of the latest documentation are 
provided as a single ZIP file archive.  Download the latest package from
the SourceForge download link and unzip to a suitable directory on your
system.

The program can be run by executing the 'uk101-n.n.n.jar' file.

For further details refer to the 'Quick Start' and 'Operation' sections
in the documentation.

Latest Improvements
-------------------
The latest release will always include various minor bug fixes.  In 
addition the following are notable improvements:

v1.4.2
 - 

v1.4.1
 - Source code moved to GitHub
 - Simplify CPU timing control
 - Improve keyboard responsiveness

v1.4.0
 - Allow setting of memory address for video, keyboard and ACIA
 - "ram.xxxx" property to allow extra RAM blocks
 - "eprom.xxxx" property to allow programmable ROMs
 - Add EraseROM utility to erase or create a ROM image
 - WEMON monitor included in package
 - Improved audio encoding and decoding
 - Add play/record indicator light to cassette recorder
 - Allow filename to be specified when saving tapes on a Mac

v1.3.0
 - More improvements to host CPU usage
 - Bug fixes for some page-0 and indexed addressing modes
 - Support emulated CPU speeds up to 4MHz
 - MONUK01 monitor included in package
 - Allow CPU speed to be changed when running
 - '@' key works correctly with CEGMON monitor 
 - Additional games: "Labyrinth", "Real-Time Star Trek" 
 - Add TapePlay utility to play tapes to the speaker
 - Generate audio waveforms to match the real hardware
 - Improve appearance in some look-and-feels (especially Mac)
 - Enable patching of ROMs when loaded

v1.2.0
 - "rom.XXXX" property to allow extra plug-in ROMs
 - Additional games: "8K Super Invaders", "Asteroids" and "Le Passe-Temps"
 - CEGMON monitor included in package
 - Default screen size to support CEGMON and MONUK02
 - Support for reading and writing Kansas City Standard audio tapes

v1.1.0
 - "baud.rate" property to allow correct ACIA timing signals
 - Greatly reduced host CPU usage 
 - Keyboard window doesn't need to be selected to type

v1.0.0
 - "keyboard=uk/us" property to support Superboard II emulation

v0.6.0:
 - Better CPU speed accuracy
 - "screen.update=async/sync" property
 - Bug fix for broken BASIC division 

Release History
---------------
v0.5.0  December 2010   First public release
v0.6.0  January 2011    Bug fixes 
v1.0.0  February 2011   Add Ohio Superboard II support
v1.1.0  October 2011    ACIA support for 8K Super Invaders
v1.2.0  January 2014    Support additional plug-in ROMs and audio encoded tapes
v1.3.0  November 2015   Index addressing mode bug fixes and more CPU speed options
v1.4.0  September 2017  Additional configuration and EPROM support 
v1.4.1  August 2021     Source code moved to GitHub
v1.4.2  May 2022        Support BASIC1 to BASIC4 and rename ePROM as NVRAM

---
Tim Baldwin
<tjb101@tinymail.co.uk>
