# SimPic16F84
> A simple software simulation of the Pic16F84 µC

## Table of Contents

- [Getting Started](#getting-started)
  - [Usage](#usage)
  - [Custom Programs](#custom-programs)
- [See also](#see-also)
- [Authors](#authors)
- [License](#license)
  - [Forbidden](#forbidden)

## Getting Started

The SimPic16F84 project is being developed as part of a practical course in Applied Computer Science at
[Offenburg University of Applied Sciences](https://www.hs-offenburg.de/). A software simulation of the
[Pic16F84](http://www.microchip.com/wwwproducts/en/PIC16F84) µC is developed.
This means the simlator will be able to interpret the basic instruction set and to simulate the OPC execution. Due to the fact that
this is a software simulation and to keep the facts simple, however compliance with CPU cycles and real time execution is waived.

The following features are partially or fully supported by the latest version of this simulator:

- Executing custom programs parsed as LST file\*
- Full support for the Pic16F84 instruction set
- Partial support for interrupts (T0, INT)
- Runtime Counter and modifyable quartz frequency
- I/O Ports (Port A, Port B)
- Timer support (TMR0)
- Debugging mode (e.g. Breakpoints)

\**For now a specific LST format is required. See [Custom Programs](#custom-programs) section for more details.*

### Usage

Releases are available in two different formats generally, as executable native installer\* and executable JAR archive. Just get the [latest release](https://github.com/0x1C1B/SimPic16F84/releases) from the download section. Please note, for executing the JAR archive a Java runtime environment is required, at least Oracle's [JRE SE 8](https://www.oracle.com/technetwork/java/javase/overview/index.html).

- Download: [SimPic16F84 Latest Version](https://github.com/0x1C1B/SimPic16F84/releases)

\**Depended to the version this kind of download is only supported for Microsoft Windows based systems. Because the JVM is already bundled inside of the installer, **no** addittional JRE is required.*

### Custom Programs

Testing/Writing custom programs with/for the simulator is quite easy. You've just to notice some points: Because a university's course
related LST parser implementation is used for now, the loaded LST file **must** match a specific format to get processed successfully.
Please note the following format is only required for lines containing machine instructions. The address as well as the instruction itself has to be formatted as hexadecimal number.

**Format:** `<Address> <Instruction> ...` e.g. `0001 3930 ...`

```
                    00000  ;Lines without machine instructions are ignored
                    00001  start    
0000 3011           00002           movlw 11h           ;Rest of line after the instruction itself doesn't matter
0001 3930           00003           andlw 30h
0002 380D           00004           iorlw 0Dh
0003 3C3D           00005           sublw 3Dh
0004 3A20           00006           xorlw 20h
0005 3E25           00007           addlw 25h
                    00008             
                    00009           
                    00010  end     
0006 2806           00011           goto end
```

## See also

- [Romux PIC Tutorial](http://romux.com/tutorials/pic-tutorial)
- [Microchip PIC16F84](https://www.microchip.com/wwwproducts/en/PIC16F84)
- [PIC Microcontrollers](http://www.islavici.ro/cursuriold/conducere%20sist%20cu%20calculatorul/PICbook/0_Uvod.htm)

## Authors

- [Freddy1096](https://github.com/Freddy1096) (University of Applied Science Offenburg)
- [0x1C1B](https://github.com/0x1C1B) (University of Applied Science Offenburg)

See also the list of [contributors](https://github.com/0x1C1B/SimPic16F84/contributors) who participated in this project.

## License

Copyright (c) 2019 0x1C1B; Freddy1096

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

[MIT License](https://opensource.org/licenses/MIT) or [LICENSE](LICENSE) for
more details.

### Forbidden

**Hold Liable**: Software is provided without warranty and the software
author/license owner cannot be held liable for damages.
