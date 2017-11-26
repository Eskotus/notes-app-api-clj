(ns create
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [helpers.db :as db]
            [helpers.responses :refer [success failure]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn now [] (new java.util.Date))

(defn handle-event [event]
;;  (pprint event)
  (let [data (json/read-str (get-in event [:body]) :key-fn keyword)
        param {:userId (get-in event [:requestContext :identity :cognitoIdentityId])
               :noteId (uuid)
               :content (get-in data [:content])
               :attachment (get-in data [:attachment])
               :createdAt (.getTime(now))}]
    (try
      (db/call "put-item" :notes param)
      (success param)
      (catch Exception e (pprint e) (failure (.getMessage e))))))

(defn -handleRequest [this is os context]
  (let [w (io/writer os)]
    (-> (json/read (io/reader is) :key-fn keyword)
        (handle-event)
        (json/write w))
    (.flush w)))
