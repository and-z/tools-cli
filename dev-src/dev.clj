(ns dev
  (:require [it.zimpel.tools.cli.core :as c]
            [it.zimpel.tools.cli.component :as cc]))

(def cli-spec [[nil "--host HOST"      "(required) HTTP Server Hostname"]
               ["-p" "--port PORT"     "(required) Datomic Database URI"
                :parse-fn #(Integer/parseInt %)
                :default 8080]])

(def greet-spec [["-n" "--name NAME"     "(required) Name of person to greet"]
                 ["-v" nil "Very Important Person?"
                  :id :vip?]])

;; Just a demo task
(defn greet [& args]
  (when-some [{:keys [name vip?]} (c/params args :cli-spec greet-spec)]
    (if vip?
      (format "Hello %s, nice to see you" name)
      (str "Hello " name))))

(comment
  (parse-long-name "--name FOO")
  (parse-long-name "--help")
  (collect-required cli-spec)
  (collect-required greet-spec)

  (c/valid-opts ["-l" "foo"] {:cli-spec cli-spec})
  (c/valid-opts ["-d" "foo" "-x"] {:cli-spec cli-spec})

  (c/valid-opts ["-p"] {:cli-spec cli-spec})
  (c/valid-opts ["-p" "1234"] {:cli-spec cli-spec})

  (c/get-valid-opts ["-p"] {:cli-spec cli-spec})
  (c/get-valid-opts ["-p" "1234"] {:cli-spec cli-spec})
  (c/get-valid-opts ["-p" "x1234" "--host" "localhost"] {:cli-spec cli-spec})

  (c/get-valid-opts ["-h"] {:cli-spec cli-spec})
  (c/get-valid-opts ["-l" "foo"] {:cli-spec cli-spec})
  (c/get-valid-opts ["-p" "1234" "--host" "localhost"] {:cli-spec cli-spec})

  (c/get-valid-opts [] {:cli-spec cli-spec})

  (cc/start [] :error-fn println :cli-spec cli-spec)
  (cc/start ["-h"] :error-fn println :cli-spec cli-spec)
  (cc/start ["-d" "abc"] {:error-fn println :cli-spec cli-spec :ok-fn println})
  (cc/start ["--host" "localhost" "-p" "123"] {:error-fn println :cli-spec cli-spec :ok-fn println})

  (cc/start ["-d" "abc"] {:cli-spec cli-spec})

  ;
  )
