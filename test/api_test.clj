(ns api_test
  (:use clojure.test
        [clojure.pprint :only (pprint)])
  (:require [create :as create]
            [get :as get]
            [list :as list]
            [update :as update]
            [clojure.core]
            [taoensso.faraday :as far]
            [environ.core :as environ]
            [clojure.data.json :as json]))

(def client-opts
  {:endpoint (environ/env :db-address)})

(defn myfixture [block]
  (do
    (far/create-table client-opts :notes
                      [:userId :s]                          ; Primary key named "id", (:n => number type)
                      {:range-keydef [:noteId :s]
                       :throughput   {:read 1 :write 1}     ; Read & write capacity (units/sec)
                       :block?       true                   ; Block thread during table creation
                       })
    (block)
    (far/delete-table client-opts :notes)))

(use-fixtures :once myfixture)

(deftest test-uuid
  (testing "Generating uuid"
    (is (string? (re-matches #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" (create/uuid))))))

(deftest test-now
  (testing "Get current time in millis"
    (is (number? (.getTime (create/now))))))

(deftest test-api-operations
  (testing "API operation: "
    (testing "Create item in db"
      (let [res (create/handle-event
                  {:body           "{\"content\":\"hello world\",\"attachment\":\"hello.jpg\"}"
                   :requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}})]
        (is (= "200" (get-in res [:statusCode])))))
    (testing "List items for user in db"
      (create/handle-event
        {:body           "{\"content\":\"hello world2\",\"attachment\":\"hello2.jpg\"}"
         :requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}})
      (let [res (list/handle-event
                  {:requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}})]
        (is (= "200" (get-in res [:statusCode])))
        (is (= 2 (count
                   (json/read-json
                     (get-in res [:body])))))))
    (testing "Get item from db"
      (let [notes (json/read-json
                    (get-in
                      (list/handle-event
                        {:requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}}) [:body]))
            note (get/handle-event
                   {:requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}
                    :pathParameters {:id (get-in notes [0 :noteId])}})]
        (is (= "200" (get-in note [:statusCode])))
        (is (= (get-in notes [0 :createdAt])
               (get-in (json/read-json
                         (get-in note [:body]))
                       [:createdAt])))))
    (testing "Get item that doesn't exist in db"
      (let [note (get/handle-event
                   {:requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}
                    :pathParameters {:id "0"}})]
        (is (= "500" (get-in note [:statusCode])))))
    (testing "Update item in db"
      (let [notes (json/read-json
                    (get-in
                      (list/handle-event
                        {:requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}}) [:body]))
            res (update/handle-event {:requestContext {:identity {:cognitoIdentityId "USER-SUB-1234"}}
                                      :pathParameters {:id (get-in notes [0 :noteId])}
                                      :body           "{\"content\":\"new world\",\"attachment\":\"new.jpg\"}"})]
        (is (= "200" (get-in res [:statusCode])))
        (is (= true (get-in
                      (json/read-json
                        (get-in res [:body]))
                      [:status])))))))
