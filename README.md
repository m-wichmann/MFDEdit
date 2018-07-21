# MFDEdit
Editor for Yamaha Music Finder (mfd) files. This tool is inspired by "Music Finder File Manager" from Jørgen Sørensen (see [1]). Since the tool was lacking some features I found it easier to implement a similar tool myself instead of dealing with this, presumably Closed Source, program. Still thanks to him for providing some info on the file format (see [2]). The main missing feature was the support for external styles, i.e. styles that aren't built in, but are stored on the harddrive, of the Tyros 5.

To extend on the format documentation in [2], here some details about "external style support".
* If style number is set to 65533, it uses an external style, that is specified in a trailer, after all the fields described in [2]
* The trailer starts with header of 6 bytes ("FPhd\0\0"), followed by two bytes that contain the length of the trailer in bytes
* Every entry in the trailer has the following parts
	* Marker ("FPdt")
	* Length of the entry in bytes (2 bytes)
	* Offset into first part of the file (2 bytes). This links the actual music finder entry to the external path.
	* Flags (1 byte). I only identified a "is_music"-flag, that is set, if the entry actually links to a midi file, instead of a style file
* The trailer is ended by the marker "FPed"

## Building
This is a eclipse project and does not have any dependencies other than Java 8, so it should be straight forward to build.

## License
This project is licensed under the MIT License. For details see LICENSE.

[1] http://www.jososoft.dk/yamaha/software/mffm/index.htm
[2] http://www.jososoft.dk/yamaha/articles/mff.htm
