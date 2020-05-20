```shell
name=somefile
if [[ -e $name.txt || -L $name.txt ]] ; then
    i=0
    while [[ -e $name-$i.txt || -L $name-$i.txt ]] ; do
        let i++
    done
    name=$name-$i
fi
touch -- "$name".txt
```