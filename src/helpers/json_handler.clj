(ns helpers.json-handler
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))

(defn handle [this is os context handle-event]
  (let [w (io/writer os)]
    (-> (json/read (io/reader is) :key-fn keyword)
        (handle-event)
        (json/write w))
    (.flush w)))