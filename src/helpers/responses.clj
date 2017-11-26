(ns helpers.responses
  (:require [clojure.data.json :as json]))

(defn- build-response [status-code body]
  ;(println status-code body))
  {:statusCode status-code
   :headers {"Access-Control-Allow-Origin" "*"
             "Access-Control-Allow-Credentials" true}
   :body (json/write-str body)})

(defn success [body]
  (build-response "200" body))

(defn failure [body]
  (build-response "500" body))
