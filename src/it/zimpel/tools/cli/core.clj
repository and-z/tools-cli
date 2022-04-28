(ns it.zimpel.tools.cli.core
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]))

(defn missing-required?
  "Returns `true` if not all elements of `required` coll are contained within
  the `options` map. For every `required` item the lookup in `options`
  (here map lookup acts as a predicate) should return the proper item.
  If a single required item is missing in `options` the function returns `false`."
  [options required]
  (not-every? options required))

(defn missing-required
  "Collect missing but required keys from `options` into a set.
  Returns an empty set, if all required keys are provided in `options`."
  [options required]
  (->> required
       (reduce (fn [acc r]
                 (if (contains? options r)
                   acc
                   (conj acc r)))
               #{})))

(defn enhanced-summary
  "Returns the enhanced `summary-str`.
  Appends potential `errors` to the result string."
  [{:keys [summary input-errors missing-args]}]
  (let [with-newlines (fn [coll] (string/join "\n" coll))
        format-errors #(->> %
                            (map (partial str "- "))
                            (with-newlines))
        lines ["Expected parameters:"
               ""
               summary
               ""]
        errors (if (seq missing-args)
                 (conj input-errors (str "Missing required parameters: " (string/join ", " missing-args)))
                 input-errors)
        error-lines ["Errors:"
                     (format-errors errors)
                     ""]
        result-lines
        (cond-> lines
          (seq errors) (into error-lines))]
    (with-newlines result-lines)))

(defn print-summary [result]
  (print (enhanced-summary result))
  (flush))

(defn valid-opts
  "Parses the seq of `args` using the provided `cli-spec`.
  Returns
  - the parsed `options` map containing the long keys with values
  - or the error map containing the `error` key and some additional hints
  - or `nil` if no options are required, no errors occurred and no args could be parsed
  "
  [args {:keys [cli-spec required]}]
  (let [{:keys [errors options summary]} (cli/parse-opts args cli-spec)
        result
        (cond
          (some? (:help options)) (merge options {:show-help? true
                                                  :summary summary})

          (seq errors) {:error :invalid-args
                        :input-errors errors
                        :summary summary}

          (missing-required? options required) {:error :missing-required-args
                                                :missing-args (missing-required options required)
                                                :summary summary}
          :else (not-empty options))]
    result))

(defn get-valid-opts
  "Parses the seq of `args` using the provided `cli-spec`. See [[valid-opts]].

  Prints the errors by default to the console. This behavior can be suppressed
  by setting `:print-summary?` key to `false`."
  [args {:as opts :keys [print-summary?] :or {print-summary? true}}]
  (let [{:as result :keys [show-help? error]} (valid-opts args opts)]
    (when (and print-summary?
               (or show-help? (some? error)))
      (print-summary result))
    result))

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
  "
  [args & {:as opts :keys [ok-fn error-fn cli-spec], :or {error-fn default-error-fn
                                                          ok-fn identity}}]
  {:pre [(some? cli-spec)]}
  (let [{:as result :keys [error]} (get-valid-opts args opts)]
    (if error
      (error-fn result)
      (ok-fn result))))
