(defproject notes-app-api-clj "0.1.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.taoensso/faraday "1.9.0"]
                 [com.amazonaws/aws-lambda-java-core "1.1.0"]
                 [com.amazonaws/aws-lambda-java-events "2.0"]
                 [environ "1.0.0"]]
  :plugins      [[lein-environ "1.0.0"]
                 [lein-dynamodb-local "0.2.10"]
                 [refactor-nrepl "1.1.0"]
                 [cider/cider-nrepl "0.9.1"]]
  :profiles     {:debug      {:debug true}
                 :dev        {:env {:clj-env :development
                                    :db-address "http://localhost:8001"}}
                 :test       {:env {:clj-env :test
                                    :db-address "http://localhost:8001"}}
                 :production {:env {:clj-env :production
                                    :db-address "http://dynamodb.us-east-1.amazonaws.com"}}}
  :dynamodb-local {:in-memory? true
                   :port 8001}
  :aot :all)
