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

```clojure
(defn readfile [f]
  "Returns a sequence from a file f"
  (with-open [rdr (clojure.java.io/reader f)]
    (doall (line-seq rdr))))

(defn write-file [to-write]
"Writes the string chain `to-write` in the file /**PATH**/graph.dot which will be used to draw the graph"
  (with-open [w (clojure.java.io/writer  "/home/elias/Documents/3I020/graphcljskel/graph.dot" :append false)]
    (.write w to-write)))
```