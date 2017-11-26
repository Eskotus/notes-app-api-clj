(ns get
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [helpers.db :as db]
            [helpers.responses :as res]))

(defn handle-event [event]
  (pprint event)
  (let [param {:userId (get-in event [:requestContext :identity :cognitoIdentityId])
               :noteId (get-in event [:pathParameters :id])}
        result (try
                 (db/call "get-item" :notes param)
                 (catch Exception e (pprint e) (res/failure {:class (str (type e)) :message (.getMessage e)})))]
    (cond
      (empty? result) (res/failure {:message "Item not found"})
      (contains? result :userId) (res/success result)
      :else result)))

(defn -handleRequest [this is os context]
  (let [w (io/writer os)]
    (-> (json/read (io/reader is) :key-fn keyword)
        (handle-event)
        (json/write w))
    (.flush w)))

;(handle-event {:requestContext
;               {:identity
;                {:cognitoIdentityId "USER-SUB-1234"}}
;               :pathParameters {:id "1c377860-3f54-4eb7-b984-453c375e0ab4"}})

;(db/call "get-item" :notes {:userId "USER-SUB-1234" :noteId "1c377860-3f54-4eb7-b984-453c375e0ab4"})
