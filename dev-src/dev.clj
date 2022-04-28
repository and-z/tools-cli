(ns dev
  (:require [it.zimpel.tools.cli.core :as c]))

(def cli-specs [["-d" "--db-uri URI"     "(required) Datomic Database URI"]
                ["-l" "--api-url URL"    "(required) Endpoint URL"]
                ["-h" "--help"           "Print this help"]])

(comment
  (c/valid-opts ["-l" "foo"] {:cli-spec cli-specs :required #{:db-uri}})
  (c/valid-opts ["-d" "foo" "-x"] {:cli-spec cli-specs :required #{:db-uri}})
  (c/get-valid-opts ["-h"] {:cli-spec cli-specs})

  (c/get-valid-opts [] {:cli-spec cli-specs :required #{:db-uri :api-url}})
  (c/get-valid-opts ["-h"] {:cli-spec cli-specs #_#_:required #{:db-uri}})

  (c/start ["-h"] :error-fn println :cli-spec cli-specs)
  (c/start ["-d" "abc"] {:error-fn println :cli-spec cli-specs})

  (c/start ["-d" "abc"] {:cli-spec cli-specs})
  (c/start ["-h"] :cli-spec cli-specs)

  ;
  )
