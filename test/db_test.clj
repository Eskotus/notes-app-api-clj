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
    (far/create-table client-opts :notes
                      [:userId :s]  ; Primary key named "id", (:n => number type)
                      {:range-keydef [:noteId :s]
                       :throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
                       :block? true ; Block thread during table creation
                      })
    (block)
    (far/delete-table client-opts :notes)))

(use-fixtures :once myfixture)

(deftest test-put-item []
  (testing "Test putting an item in db"
    (let [res (db/call "get-item" :notes {:userId "USER-SUB-1234" :noteId "1c377860-3f54-4eb7-b984-453c375e0ab4"})]
      (is (nil? res)))))

;; (far/list-tables client-opts)

;; (far/create-table client-opts :notes
;;                       [:userId :s]  ; Primary key named "id", (:n => number type)
;;                       {:range-keydef [:noteId :s]
;;                        :throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
;;                        :block? true ; Block thread during table creation
;;                       })

;; (far/delete-table client-opts :notes)
