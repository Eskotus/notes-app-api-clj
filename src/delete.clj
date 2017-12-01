(ns delete
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [helpers.db :as db]
            [helpers.responses :as res]
            [helpers.json-handler :as jh]))

(defn handle-event [event]
  ;(pprint event)
  (let [param {:userId (get-in event [:requestContext :identity :cognitoIdentityId])
               :noteId (get-in event [:pathParameters :id])}]
    (try
      (db/call "delete-item" :notes param)
      (res/success {:status true})
      (catch Exception e (pprint e) (res/failure {:status false})))))

(defn -handleRequest [this is os context]
  (jh/handle this is os context handle-event))

