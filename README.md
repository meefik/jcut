jcut
====

Copyright (C) 2013-2015 Anton Skshidlevsky

Licensed under the [GPL version 3](http://www.gnu.org/licenses/) or later.

Консольное Java приложение для отсеживания изменений в каталоге. Запускается два раза, до изменений и после них. Первый запуск создает снимок каталога, а последующие запуски отображают произошедшие изменения.

Usage:

    $ java -jar jcut.jar <directory> [snapshot.gz]

Example:

    $ java -jar jcut.jar /path/to/dir
    add	/test/dir1	0
    mod	/test/aaa	1433802751000
    del	/test/dir2/file1	1433802787000
    del	/test/dir2	0
    Time: 3742 ms
    Processed: 209846 items
