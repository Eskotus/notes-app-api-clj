(ns helpers.db
  (:require [taoensso.faraday]
            [environ.core :as environ]))

(def client-opts
  {:endpoint (environ/env :db-address)})

(defn call [action table param]
  (if (string? action) ((ns-resolve 'taoensso.faraday (symbol action)) client-opts table param)))

; tests
;(call "get-item" :notes {:userId "USER-SUB-1234" :noteId "1c377860-3f54-4eb7-b984-453c375e0ab4"})

;(call "get-item" :notes {:userId "USER-SUB-1234" :noteId "1c377860-3f54-4eb7-b984-453"})
