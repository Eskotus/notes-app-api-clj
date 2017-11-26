(ns create_test
  (:use clojure.test)
  (:require [create :as create]
            [get :as get]
            [clojure.core]
            [taoensso.faraday :as far]
            [environ.core :as environ]))

(def client-opts
  {:endpoint (environ/env :db-address)})

(defn myfixture [block]
  (do
    (far/create-table client-opts :notes
                      [:userId :s]  ; Primary key named "id", (:n => number type)
                      {:range-keydef [:noteId :s]
                       :throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
                       :block? true ; Block thread during table creation
                      })
        (block)
        (far/delete-table client-opts :notes)))

(use-fixtures :once myfixture)

(deftest test-uuid []
  (testing "Generating uuid"
    (is (string? (re-matches #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" (create/uuid))))))

(deftest test-now []
  (testing "Get current time in millis"
    (is (number? (.getTime (create/now))))))

(deftest test-handle-event []
  (testing "Test creating an item in db"
    (let [res (create/handle-event
               {:body "{\"content\":\"hello world\",\"attachment\":\"hello.jpg\"}"
                :requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}})]
      (is (= "200" (get-in res [:statusCode]))))))

;; (far/list-tables client-opts)

;; (far/create-table client-opts :notes
;;                       [:userId :s]  ; Primary key named "id", (:n => number type)
;;                       {:range-keydef [:noteId :s]
;;                        :throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
;;                        :block? true ; Block thread during table creation
;;                       })

;; (far/delete-table client-opts :notes)
