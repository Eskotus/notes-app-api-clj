(ns create
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [helpers.db :as db]
            [helpers.responses :refer [success failure]]
            [helpers.json-handler :as jh])
  (:import (java.util Date
                      UUID)))

(defn uuid [] (str (UUID/randomUUID)))

(defn now [] (-> (new Date) (.getTime)))

(defn handle-event [event]
  (let [data (json/read-str (get-in event [:body]) :key-fn keyword)
        param {:userId     (get-in event [:requestContext :identity :cognitoIdentityId])
               :noteId     (uuid)
               :content    (get-in data [:content])
               :attachment (get-in data [:attachment])
               :createdAt  (now)}]
    (try
      (db/call "put-item" :notes param)
      (success param)
      (catch Exception e (pprint e) (failure (.getMessage e))))))

(defn -handleRequest [this is os context]
  (jh/handle this is os context handle-event))
