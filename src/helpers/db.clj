(ns helpers.db
  (:require [taoensso.faraday]
            [environ.core :as environ]))

(def client-opts
  {:endpoint (environ/env :db-address)})

(defn call [action table param & [opts]]
  (if (string? action) ((ns-resolve 'taoensso.faraday (symbol action)) client-opts table param opts)))
