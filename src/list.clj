(ns list
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
  (let [param {:userId [:eq (get-in event [:requestContext :identity :cognitoIdentityId])]}]
    (try
      (res/success
        (db/call "query" :notes param))
      (catch Exception e (pprint e) (res/failure {:error {:class (str (type e)) :message (.getMessage e)}})))))

(defn -handleRequest [this is os context]
  (jh/handle this is os context handle-event))

