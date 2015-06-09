jcut
====

Copyright (C) 2013-2015 Anton Skshidlevsky

Licensed under the [GPL version 3](http://www.gnu.org/licenses/) or later.

Java console application for tracking changes in the directory. It is started twice before and after the changes. When you first start is created a snapshot of the directory, and subsequent launches reflect the changes.

Usage:

    $ java -jar jcut.jar <directory> [snapshot.gz]

Example:

    $ java -jar jcut.jar /path/to/dir
    mod	/file1	1433802751000
    add	/dir1	0
    del	/dir2	0
    del	/dir2/file1	1433802787000
    Time: 3742 ms
    Processed: 209846 items

Output: operation / path / modification time (ms)
