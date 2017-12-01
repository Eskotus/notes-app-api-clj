(ns db_test
  (:use clojure.test)
  (:require [helpers.db :as db]
            [clojure.core]
            [taoensso.faraday :as far]
            [environ.core :as environ]))

(def client-opts
  {:endpoint (environ/env :db-address)})

(defn myfixture [block]
  (do
    (far/create-table client-opts :test
                      [:userId :s]                          ; Primary key named "id", (:n => number type)
                      {:range-keydef [:noteId :s]
                       :throughput   {:read 1 :write 1}     ; Read & write capacity (units/sec)
                       :block?       true                   ; Block thread during table creation
                       })
    (block)
    (far/delete-table client-opts :test)))

(use-fixtures :once myfixture)

(deftest test-db-operations
  (testing "DB operation:"
    (testing "Put item"
      (let [res (db/call "put-item" :test {:userId "USER-SUB-1234" :noteId "1" :testData "test-data"})]
        (is (nil? res))))
    (testing "Get item"
      (let [res (db/call "get-item" :test {:userId "USER-SUB-1234" :noteId "1"})]
        (is (= "test-data" (get-in res [:testData])))))
    (testing "List items"
      (db/call "put-item" :test {:userId "USER-SUB-1234" :noteId "2" :testData "test-data"})
      (db/call "put-item" :test {:userId "USER-SUB-1234" :noteId "3" :testData "test-data"})
      (let [res (db/call "query" :test {:userId [:eq "USER-SUB-1234"]})]
        (is (= 3 (count res)))))
    (testing "Update item"
      (let [res (db/call "update-item" :test {:userId "USER-SUB-1234" :noteId "1"}
                         {:update-expr "SET testData = :testData"
                          :expr-attr-vals {":testData" "new-test-data"}
                          :return :all-new})]
        (is (= "new-test-data" (get-in res [:testData])))))))

