(ns ^{:doc "This ns provides some convenience for CLI-based applications.

The [[start]] function can be used in context of a system or component in
combination with 'success' or 'error' callback functions provided to it.

The developer has to provide CLI specification in a format understood by `clojure.tools.cli`.
Then the user input will be parsed and the appropriate callback function
will be called with the result of operation.
"}
    it.zimpel.tools.cli.component
  (:require [it.zimpel.tools.cli.core :as core]))

(defn default-error-fn
  "Prints the error to the console and exits the JVM process."
  [e]
  (println (format "\nAbort startup due to %s" e)))

(defn default-exit-fn
  "Prints the error to the console and exits the JVM process."
  [e]
  (default-error-fn e)
  (System/exit 1))

(defn start
  "Parses the seq of `args` and calls the `ok-fn` with the parsed options as an argument.
  If the provided `args` couldn't be parsed, then calls the `error-fn` with a map containing
  the error.

  `opts` is a map of keys:
  {:cli-spec [,,,]            ;; clojure.tools.cli specification of supported args
   :required #{,,,}           ;; set of required (long) keys
   :print-summary? false      ;; defaults to true, can be suppressed
   :ok-fn (fn [opts])         ;; single-arity fn which expects parsed args
   :error-fn (fn [error-map]) ;; single-arity fn which expects the error map}

  The custom `error-fn` is optional. The default implementation [[default-error-fn]]
  prints the error to the console.

  ----------------------------------------------------------------------

  Usage example:

  (require '[it.zimpel.tools.cli.component :as cli]
           '[com.stuartsierra.component :as component])

  (def cli-specs
    [[nil \"--host HOST\"    \"(required) HTTP Server Hostname\"]
     [\"-p\" \"--port PORT\"   \"(required) Datomic Database URI\"
      :parse-fn #(Integer/parseInt %)
      :default 8080]
     [\"-h\" \"--help\"        \"Print this help\"]])

  (defn new-system [opts]
   ;; use `opts` to create new system
  )

  (defn try-start [opts]
    (println \"Start system with opts:\" opts)
    (component/start (new-system opts)))

  (defn -main [& args]
    (cli/start args :ok-fn try-start :error-fn cli/default-exit-fn))
  "
  [args & {:as opts :keys [ok-fn error-fn cli-spec], :or {error-fn default-error-fn
                                                          ok-fn identity}}]
  {:pre [(some? cli-spec)]}
  (let [{:as result :keys [error]} (core/valid-opts args opts)]
    (if error
      (error-fn result)
      (ok-fn result))))
