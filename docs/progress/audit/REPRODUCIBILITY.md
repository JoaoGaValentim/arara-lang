# D001 audit — reproducibility record (Directive 001-R2)

Every claim in `D001-AUDIT-R2.md` that is marked `VERIFIED` has a command in this
file and an observed result. Anything not reproducible here is labeled
`UNVERIFIED` in the report.

Environment for every run below (re-measured in this session, not copied):

| Item | Value | Command |
| --- | --- | --- |
| OS | macOS 27.0.1, Darwin 27.0.0, arm64 | `sw_vers`, `uname -a` |
| JDK | Corretto 21.0.12.1, build 21.0.12.1+12-LTS | `java -version` |
| javac | 21.0.12.1 | `javac -version` |
| Gradle | 9.8.1, launcher JVM 21.0.12.1 | `gradle --version` |
| Git | 2.54.0 (Apple Git-157) | `git --version` |
| GitHub CLI | 2.102.0, authenticated as `JoaoGaValentim` | `gh --version`, `gh auth status` |
| Kilo CLI | 7.8.12 | `kilo --version` |
| Python | 3.14.8 (audit tooling only) | `python3 --version` |
| ASM (published) | 9.11.1 is the newest on Maven Central | `maven-metadata.xml` |
| JUnit Jupiter (published) | 6.1.3 is the newest on Maven Central | `maven-metadata.xml` |

Audit tooling lives **outside** the repository, in `$TMPDIR/d001audit`. No script
is committed, because `D001-G1` forbids `.py`/`.java` sources in this milestone.
The LL(1) checker is therefore reproduced as a listing in §6 so it can be retyped
verbatim.

## 1. Git baseline and remote state

```sh
git rev-parse HEAD
git branch --show-current
git status --porcelain=v1 --untracked-files=all
git branch -vv
git rev-parse --abbrev-ref --symbolic-full-name @{u}
git remote -v
git ls-remote origin
gh repo view JoaoGaValentim/arara-lang --json isPrivate,isEmpty,visibility
git fsck --no-progress
```

Observed:

```text
82a7afa93359848801388c5def63c2fd7ed0a147
docs/d001-r1-arara-identity
(empty)
* docs/d001-r1-arara-identity 82a7afa checkpoint(d001-r1): pin ledger to ce3c67d
  main                        717fcc6 checkpoint(d001): record verified commit after audit resolution
fatal: no upstream configured for branch 'docs/d001-r1-arara-identity'
origin  https://github.com/JoaoGaValentim/arara-lang.git (fetch/push)
(no refs)
{"defaultBranchRef":{"name":""},"isEmpty":true,"isPrivate":true,"visibility":"PRIVATE"}
(fsck silent)
```

Branch divergence: exactly two commits ahead of `main`
(`ce3c67d`, `82a7afa`); `main` has 7 commits. No tags, no `packed-refs`.

## 2. Repository inventory (implementation-absence gate)

```sh
git ls-files | wc -l
git ls-files | sed 's/.*\.//' | sort | uniq -c
git ls-files | grep -Ei '\.(py|java|kt|rs|gradle|xml)$'
```

Observed: 24 tracked files — 21 `.md`, 1 `.ebnf`, 1 `.jsonc`, 1 `.gitignore`.
No `.py`, `.java`, `.kt`, `.rs`, `.gradle`, `.xml`; no `src/`, `gradlew`,
`pom.xml`. `D001-G1` holds at `82a7afa`.

## 3. D001-R1 diff scope

```sh
git show --stat ce3c67d
git diff --shortstat 717fcc6 ce3c67d
git show 717fcc6:docs/spec/syntax.ebnf | awk '/```ebnf/{f=1} f' > /tmp/a
git show ce3c67d:docs/spec/syntax.ebnf | awk '/```ebnf/{f=1} f' > /tmp/b
diff /tmp/a /tmp/b
```

Observed: 12 files changed, **+332/−95**, matching the delivered D001-R1 report;
`diff` of the two EBNF grammar blocks is empty, so the production set is
byte-identical across D001-R1. All 12 changes are documentation or Kilo
configuration.

## 4. LL(1) re-derivation

Independent checker (§6), run as
`python3 ll1check.py docs/spec/syntax.ebnf`:

```text
productions: 27
nullable declared: ['FunctionTail', 'StatementTail']
LL(1) conflicts: 0
```

Full FIRST/FOLLOW output is reproduced in `D001-AUDIT-R2.md` §4. The checker
lowers EBNF (`[ ]`, `{ }`, `|`) into 59 BNF productions including helper
nonterminals, computes nullable/FIRST/FOLLOW to a fixed point, and compares
selection sets pairwise for every alternative of every nonterminal.

Additional structural checks:

```sh
python3 -c "reachability + undefined-reference scan over the ```ebnf block"
```

Observed: 27 declared productions, 27 reachable from `Program`, 0 unreachable,
0 undefined nonterminals. `EOF`, `IDENT`, `SEMI` are terminals declared in
`lexical.md` §3, not undefined references.

## 5. Reserved-word audit

```sh
python3 -c "extract lexical.md §6 rules 29-31"
```

Observed: 12 keywords (rule 29) and **28** reserved words (rule 31), all mapped
to a `docs/charter.md` §4 category. Cross-checks:

- Grammar keyword literals: `else false fun if return true val var` — no reserved
  word appears as a grammar terminal.
- 12 `arara` code fences scanned: no reserved word appears as an identifier.
- `yield` is **not** reserved by Arara, while JLS 21 lists it as a contextual
  keyword. See `D001-AUDIT-R2.md` §6.

## 6. LL(1) checker listing (retype verbatim to reproduce)

```python
# ll1check.py — usage: python3 ll1check.py docs/spec/syntax.ebnf
import re, sys
src = open(sys.argv[1]).read()
g = re.search(r'```ebnf\n(.*?)```', src, re.S).group(1)
TERMS = {'IDENT', 'INT_LITERAL', 'STRING_LITERAL', 'SEMI', 'EOF'}

prods, order, cur = {}, [], None
for raw in g.splitlines():
    line = raw.strip()
    if not line:
        continue
    m = re.match(r'^([A-Za-z_]\w*)\s*::=\s*(.*)$', line)
    if m:
        cur = m.group(1); order.append(cur); prods[cur] = m.group(2)
    else:
        prods[cur] += ' ' + line
for k in prods:
    prods[k] = prods[k].rstrip().rstrip('.').strip()

def lex(rhs):
    out, i = [], 0
    while i < len(rhs):
        c = rhs[i]
        if c.isspace(): i += 1
        elif rhs.startswith('(*', i): i = rhs.index('*)', i) + 2
        elif c == '"':
            j = rhs.index('"', i + 1); out.append(('LIT', rhs[i+1:j])); i = j + 1
        elif c in '|()[]{}': out.append((c, None)); i += 1
        else:
            m = re.match(r'[A-Za-z_]\w*', rhs[i:])
            word = m.group(0); out.append(('TERM' if word in TERMS else 'NT', word)); i += len(word)
    return out

class P:
    def __init__(s, t): s.t, s.i = t, 0
    def pk(s): return s.t[s.i] if s.i < len(s.t) else ('END', None)
    def eat(s, k):
        tok = s.pk(); assert tok[0] == k, (k, tok); s.i += 1
    def alt(s):
        br = [s.seq()]
        while s.pk()[0] == '|': s.eat('|'); br.append(s.seq())
        return ('alt', br) if len(br) > 1 else br[0]
    def seq(s):
        it = []
        while s.pk()[0] in ('LIT', 'NT', 'TERM', '(', '[', '{'): it.append(s.atom())
        return ('eps',) if not it else ('seq', it) if len(it) > 1 else it[0]
    def atom(s):
        k, v = s.pk()
        if k in ('LIT', 'NT', 'TERM'): s.eat(k); return (k, v)
        s.eat(k); a = s.alt(); s.eat({ '(': ')', '[': ']', '{': '}' }[k])
        return {'(': a, '[': ('opt', a), '{': ('star', a)}[k]

rules, n = {nt: [] for nt in prods}, [0]
def helper(b):
    n[0] += 1; nt = '%s#%d' % (b, n[0]); rules[nt] = []; return nt
def emit(a, b, out):
    k = a[0]
    if k == 'eps': return
    if k in ('LIT', 'TERM'): out.append(('T', a[1])); return
    if k == 'NT': out.append(a[1]); return
    if k == 'seq':
        for x in a[1]: emit(x, b, out)
        return
    if k == 'alt':
        h = helper(b)
        for br in a[1]:
            r = []; emit(br, b, r); rules[h].append(r)
        out.append(h); return
    if k == 'opt':
        h = helper(b); r = []; emit(a[1], b, r)
        rules[h].append(r); rules[h].append([]); out.append(h); return
    if k == 'star':
        h = helper(b); r = []; emit(a[1], b, r)
        rules[h].append([]); rules[h].append(r + [h]); out.append(h); return
for nt in order:
    p = P(lex(prods[nt])); r = []; emit(p.alt(), nt, r); rules[nt].append(r)

nullable = set()
changed = True
while changed:
    changed = False
    for nt, alts in rules.items():
        if nt in nullable: continue
        for rhs in alts:
            if all((not isinstance(s, tuple)) and s in nullable for s in rhs):
                nullable.add(nt); changed = True; break

def first_of(sym, stack):
    if isinstance(sym, tuple): return frozenset([sym[1]])
    if sym in stack: return frozenset()
    out, st = set(), stack | {sym}
    for rhs in rules[sym]:
        if not rhs: out.add('EPS'); continue
        for s in rhs:
            f = first_of(s, st); out |= set(f - {'EPS'})
            if 'EPS' not in f: break
        else: out.add('EPS')
    return frozenset(out)

FIRST = {nt: set(first_of(nt, frozenset()) - {'EPS'}) for nt in rules}
FOLLOW = {nt: set() for nt in rules}; FOLLOW['Program'].add('$')
changed = True
while changed:
    changed = False
    for nt, alts in rules.items():
        for rhs in alts:
            for i, s in enumerate(rhs):
                if isinstance(s, tuple): continue
                f = set()
                for x in rhs[i+1:]:
                    fx = first_of(x, frozenset()); f |= set(fx - {'EPS'})
                    if 'EPS' not in fx: break
                else: f |= FOLLOW[nt]
                old = len(FOLLOW[s]); FOLLOW[s] |= f
                if len(FOLLOW[s]) != old: changed = True

def sel(rhs, nt):
    out = set()
    for s in rhs:
        f = first_of(s, frozenset()); out |= set(f - {'EPS'})
        if 'EPS' not in f: return out
    return out | FOLLOW[nt]

conf = [(nt, a, b, sorted(sel(a, nt) & sel(b, nt)))
        for nt, alts in rules.items()
        for a, b in ((alts[i], alts[j]) for i in range(len(alts)) for j in range(i+1, len(alts)))
        if sel(a, nt) & sel(b, nt)]

print('productions:', len(prods))
print('nullable declared:', sorted(n for n in order if n in nullable))
print('LL(1) conflicts:', len(conf))
for c in conf: print('  CONFLICT', c)
for nt in order:
    print('%-24s FIRST=%-62s FOLLOW=%s' % (nt, sorted(FIRST[nt]), sorted(FOLLOW[nt])))
```

Known limitation of this checker: binary-operator `{ ... }` repetitions are
lowered to right-recursive helpers, which is sound for selection-set comparison
but is not itself a parser. A D002 implementation should use its own automated
FIRST/FOLLOW test over the committed grammar (`D002-G10`).

## 7. JVM and Java-source probes

All probes used hand-built or `javac`-built class files outside the repository.
Class files were emitted for major version 65 and loaded with `-Xverify:all`.

### 7.1 Class names

```sh
javac <Name>.java            # source-level acceptance
java -Xverify:all -cp . <Name>
```

| Name | `javac` accepts as class name | JVM accepts under `-Xverify:all` |
| --- | --- | --- |
| `var`, `yield`, `record`, `sealed`, `permits` | no — `not allowed here` | yes |
| `module`, `open`, `requires`, `transitive`, `exports`, `opens`, `to`, `uses`, `provides`, `with` | yes | yes |
| `when` | yes | yes |
| `_` | no — `as of release 9, '_' is a keyword` | yes |
| `String`, `Object`, `java`, `Int`, `Boolean`, `Unit`, `fun`, `val` | yes | yes |
| `if`, `int`, `class`, `void`, `null`, `goto`, `const`, `switch`, `do`, `true`, `false`, `for`, `else`, `return` | no | yes |

### 7.2 Method names

Hand-built class with `public static` methods named `if`, `for`, `class`, `_`,
`var`, `fun`, `Int`, `switch`, `goto`: all load and execute under
`-Xverify:all`. A `javac` client referencing `K.int()` fails with
`<identifier> expected`, so keyword method names are JVM-valid but not
Java-source-callable.

### 7.3 JDK class-file API

```sh
java -cp . probe_classfile     # Class.forName("java.lang.classfile.ClassFile")
```

Observed: `ABSENT: java.lang.ClassNotFoundException`,
`runtime=21.0.12.1 vendor=Amazon.com Inc.` — ADR 0002's correction is accurate.

### 7.4 Verifier defaults and strength

```sh
java -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal -version | grep BytecodeVerification
```

Observed: `BytecodeVerificationLocal = false`, `BytecodeVerificationRemote = true`.

Malformed classes (`areturn` / `ireturn` in a `void` method) were rejected under
**default** flags and under `-Xverify:all` with
`java.lang.VerifyError: Operand stack underflow`. `-Xverify:none` bypassed the
verifier (deprecation warning only). Consequence: for the malformed shapes probed,
the default verifier is sufficient; `-Xverify:all` remains the correct gate
because it does not depend on that observation generalizing.

### 7.5 `java.lang` shadowing by a unit class

```sh
javac -cp . -d . A.java     # A.java uses fully qualified java.lang.String / java.lang.System
java -Xverify:all -cp . java
```

| Unit class present | `javac` result on `A.java` | JVM result |
| --- | --- | --- |
| none (control) | compiles | runs |
| `java.class` | **fails** — `cannot find symbol: class lang` | runs |

Additional cases: with `System.class` present, `javac` rejects `System.out`
(`cannot find symbol: variable out`); with `String.class` present, `javac` rejects
`String s = "hi"` (`java.lang.String cannot be converted to String`). Arara's own
emitted code always uses internal names (`java/lang/String`), so this is a
Java-source hazard only, but it breaks the D005 interop premise for such units.

### 7.6 Local slot 0 and entry bridge

```sh
javac -g -d . S.java && javap -l -p -c S
```

Observed: for `public static void main()` with one local, `javap` shows
`istore_0` / `iload_0` — slot 0 is **used** by the first local in a
zero-parameter static method. A hand-built method that reserves slot 0 and uses
slot 1 for its only local also loads and returns correctly under `-Xverify:all`,
so reserving slot 0 is *legal* but is not what `javac` does.

Entry bridge probe: a class with `main()V` plus `main([Ljava/lang/String;)V` that
delegates prints `arara-main` under `-Xverify:all`; a third overload
`main(String[], int)` coexists without collision.

### 7.7 `CheckClassAdapter`

`CheckClassAdapter.verify` (ASM 9.7.1, data-flow analysis) reported no analysis
output for a constructor-free class, a method sharing its class name, and classes
named `var` / `record`. ASM 9.7.1 was used for the probe; the ADR pins 9.11.1 for
D002 and this probe does not change that.

### 7.8 Arithmetic and equality behaviour

```sh
java -cp . I
```

Observed: `minDivNeg1=-2147483648`, `equals=true identity=true` for
`"arara"` vs `"ar"+"ara"` (compile-time constant folding makes the identity check
`true` for literals, which is why `semantics.md` §7.4.3 correctly specifies
`String.equals` and not `==`), and `threw=java.lang.ArithmeticException` for
`1/0`.

## 8. Repository documentation checks

```sh
grep -rnE ' +$' --include='*.md' --include='*.ebnf' --include='*.jsonc' .
python3 -c "backticked-path and Markdown-link resolver"
python3 -c "diagnostic code census"
python3 -c "jsonc strict parse after comment-stripping"
```

Observed: no trailing whitespace; no missing backticked paths; no broken relative
links; 67 distinct `ARA-*` codes, each table-defined exactly once, 0 duplicates,
0 referenced-but-undefined (before this audit's edits); `.kilo/kilo.jsonc` parses
as strict JSON after removing `//` comments.

Kilo configuration checks:

```sh
kilo config check
kilo debug config
kilo debug agent code
kilo models hive --verbose
```

Observed: `No config warnings.`; `compaction` resolves
`{"auto":true,"threshold_percent":70,"prune":true,"tail_turns":4,"preserve_recent_tokens":8000,"reserved":20000}`;
three `.kilo/rules/*.md` instruction origins; `agent.code` →
`hive/deepseek-ai/deepseek-v4.1-flash-1017`, `variant: max`; model limits
`context 1048576`, `output 1048576`.

## 9. Compaction evidence

```sh
sqlite3 ~/.local/share/kilo/kilo.db \
  "SELECT DISTINCT type FROM event WHERE type LIKE '%compact%'"
```

Observed: zero rows. No Kilo session row for this repository has a non-null
`time_compacting`. **Automatic compaction has still never been observed firing.**
The `UNVERIFIED` label in `STATE.md` limitation 2 is correct and is preserved.


## 9a. D001-R3 verification checkers

Directive 001-R3 replaced the two "ad-hoc audit script (not committed)" rows in
`STATE.md` with executed commands. The listings below are the exact programs. All
run from a scratch directory outside the repository, because `D001-G1` forbids a
tracked `.py` source. Paths assume the repository root is `$REPO`.

### 9a.1 `ll1final.py` — grammar lint, FIRST/FOLLOW, selection sets

Consumes `bnf2.txt`, the EBNF lowered to BNF by `ll1v5.py`. Neither file is
tracked; both are retypeable from this listing. Result on the D001-R3 tree:
53 nonterminals, 97 productions, nullable `{FunctionTail, StatementTail}`,
0 unreachable, 0 undefined, **0 LL(1) conflicts**, 38 terminals.

```python
import ast,collections
bnf=ast.literal_eval(open('bnf2.txt').read())
bnf={k:[list(a) for a in v] for k,v in bnf.items()}
NON=set(bnf)
nullable=set(); ch=True
while ch:
    ch=False
    for nt,alts in bnf.items():
        for items in alts:
            if len(items)==0 or all(t=='N' and v in nullable for t,v in items):
                if nt not in nullable: nullable.add(nt); ch=True
                break
FIRST=collections.defaultdict(set); ch=True
while ch:
    ch=False
    for nt,alts in bnf.items():
        for items in alts:
            if not items:
                if 'EPS' not in FIRST[nt]: FIRST[nt].add('EPS'); ch=True
                continue
            for t,v in items:
                if t=='T':
                    if v not in FIRST[nt]: FIRST[nt].add(v); ch=True
                    break
                add=FIRST[v]-{'EPS'}
                if not add<=FIRST[nt]: FIRST[nt]|=add; ch=True
                if v not in nullable: break
            else:
                if 'EPS' not in FIRST[nt]: FIRST[nt].add('EPS'); ch=True
FOLLOW=collections.defaultdict(set); FOLLOW['Program'].add('EOF'); ch=True
while ch:
    ch=False
    for nt,alts in bnf.items():
        for items in alts:
            for i,(t,v) in enumerate(items):
                if t!='N': continue
                rest=items[i+1:]; fr=set(); an=True
                for t2,v2 in rest:
                    if t2=='T': fr.add(v2); an=False; break
                    fr|=FIRST[v2]-{'EPS'}
                    if v2 not in nullable: an=False; break
                if not fr<=FOLLOW[v]: FOLLOW[v]|=fr; ch=True
                if an and not FOLLOW[nt]<=FOLLOW[v]: FOLLOW[v]|=FOLLOW[nt]; ch=True
reach={'Program'}; st=['Program']
while st:
    x=st.pop()
    for items in bnf[x]:
        for t,v in items:
            if t=='N' and v not in reach: reach.add(v); st.append(v)
conf=[]
for nt,alts in bnf.items():
    sels=[]
    for items in alts:
        s=set(); an=True
        for t,v in items:
            if t=='T': s.add(v); an=False; break
            s|=FIRST[v]-{'EPS'}
            if v not in nullable: an=False; break
        if an: s|=FOLLOW[nt]
        sels.append(s)
    for a in range(len(sels)):
        for b in range(a+1,len(sels)):
            ov=sels[a]&sels[b]
            if ov: conf.append((nt,a,b,sorted(ov)))
print('nonterminals',len(NON),'productions',sum(len(v) for v in bnf.values()))
print('nullable',sorted(nullable))
print('unreachable',sorted(NON-reach))
print('undefined',sorted({v for al in bnf.values() for it in al for t,v in it if t=='N' and v not in NON}))
print('LL(1) conflicts',len(conf))
```

### 9a.2 `ll1v5.py` — EBNF to BNF lowering

Three details are easy to get wrong and each changes the verdict. `{ X }` must be
lowered **right-recursively**; left recursion manufactures a false conflict.
Alternation must be split **outside** quotes and brackets, or `"||"` becomes two
empty alternatives. Multi-line productions must be joined before splitting.
Result: 53 nonterminals, 97 productions.

```python
import re,ast
src=open('$REPO/docs/spec/syntax.ebnf').read()
g=re.search(r'```ebnf\n(.*?)```',src,re.S).group(1)
g=re.sub(r'\(\*.*?\*\)','',g,flags=re.S)
lines=g.splitlines(); rules=[]; cur=None
for ln in lines:
    if not ln.strip(): continue
    if '::=' in ln:
        if cur: rules.append(cur)
        cur=ln.strip()
    elif cur is not None: cur+=' '+ln.strip()
if cur: rules.append(cur)
def split_alts(rhs):
    out=[]; buf=''; inq=False; depth=0
    for ch in rhs:
        if ch=='"': inq=not inq; buf+=ch; continue
        if not inq:
            if ch in '([{': depth+=1
            elif ch in ')]}': depth-=1
            elif ch=='|' and depth==0: out.append(buf); buf=''; continue
        buf+=ch
    out.append(buf); return [a.strip() for a in out]
prods={}
for r in rules:
    lhs,rhs=r.split('::=',1)
    rhs=re.sub(r'\s*\.\s*$','',rhs.strip())
    prods.setdefault(lhs.strip(),[]).extend(split_alts(rhs))
NON=set(prods)
def classify(rhs):
    out=[];i=0
    while i<len(rhs):
        c=rhs[i]
        if c.strip()=='': i+=1; continue
        if c=='"':
            j=rhs.index('"',i+1); out.append(('T',rhs[i+1:j])); i=j+1; continue
        m=re.match(r'[A-Za-z_][A-Za-z0-9_]*',rhs[i:])
        if m:
            w=m.group(0); i+=len(w)
            out.append(('N',w) if w in NON else ('T',w)); continue
        out.append(('P',c)); i+=1
    return out
bnf={}; ctr=[0]
def newname(b):
    ctr[0]+=1; return f'{b}#{ctr[0]}'
def expand(toks, base):
    out=[]; i=0
    while i<len(toks):
        t,v=toks[i]
        if t=='P' and v in '[{(':
            closer={'[':']','{':'}','(':')'}[v]
            depth=1; j=i+1; inner=[]
            while j<len(toks):
                if toks[j]==('P',v): depth+=1
                elif toks[j]==('P',closer):
                    depth-=1
                    if depth==0: break
                inner.append(toks[j]); j+=1
            if v=='[':
                h=newname(base+'_opt'); bnf[h]=[expand(inner,base),[]]
            elif v=='{':
                h=newname(base+'_star')
                bnf[h]=[expand(inner,base)+[('N',h)],[]]
            else:
                h=newname(base+'_grp')
                alts=[]; cur2=[]; d2=0
                for sym in inner:
                    if sym[0]=='P' and sym[1] in '([{': d2+=1
                    if sym[0]=='P' and sym[1] in ')]}': d2-=1
                    if sym[0]=='P' and sym[1]=='|' and d2==0:
                        alts.append(cur2); cur2=[]; continue
                    cur2.append(sym)
                alts.append(cur2)
                bnf[h]=[expand(a,base) for a in alts]
            out.append(('N',h)); i=j+1; continue
        out.append((t,v)); i+=1
    return out
for nt,alts in prods.items():
    bnf.setdefault(nt,[])
    for a in alts: bnf[nt].append(expand(classify(a),nt))
open('bnf2.txt','w').write(repr(bnf))
print('nonterminals',len(bnf),'rules',sum(len(v) for v in bnf.values()))
```

### 9a.3 `traceaudit.py` — diagnostic definition and reference audit

Definitions are the rows of the diagnostic tables in `lexical.md`,
`syntax.ebnf`, and `semantics.md`. Result on the D001-R3 tree: **68** defined
codes (LEX 12, PARSE 19, SRC 5, NAME 11, TYPE 15, JVM 6), **0** duplicated
definitions, 68 referenced, **0** referenced but undefined.

```python
import re,glob,collections,os
R=os.environ['REPO']
DEF=re.compile(r'^\|\s*`(ARA-([A-Z]+)-(\d{4}))`\s*\|',re.M)
defs=collections.defaultdict(list)
for rel in ['docs/spec/lexical.md','docs/spec/syntax.ebnf','docs/spec/semantics.md']:
    for m in DEF.finditer(open(os.path.join(R,rel)).read()):
        defs[m.group(1)].append(rel)
print('total defined',len(defs))
print('duplicated',[c for c,v in defs.items() if len(v)>1])
print('by namespace',dict(collections.Counter(c.split('-')[1] for c in defs)))
full=re.compile(r'ARA-([A-Z]+)-(\d{4})')
refs=collections.defaultdict(set)
for f in glob.glob(R+'/**/*.md',recursive=True)+glob.glob(R+'/**/*.ebnf',recursive=True):
    for line in open(f):
        if DEF.match(line): continue
        for m in full.finditer(line): refs[m.group(0)].add(os.path.relpath(f,R))
print('referenced',len(refs))
print('referenced but UNDEFINED',sorted(c for c in refs if c not in defs))
```

### 9a.4 `g12check.py` — one obligation per defined code

Result: **68 of 68** codes carry at least one obligation in
`conformance-plan.md`. Zero uncovered.

```python
import re,os
R=os.environ['REPO']
defs={}
for rel in ['docs/spec/lexical.md','docs/spec/syntax.ebnf','docs/spec/semantics.md']:
    for m in re.finditer(r'^\|\s*`(ARA-[A-Z]+-\d{4})`\s*\|',open(os.path.join(R,rel)).read(),re.M):
        defs[m.group(1)]=rel
conf=open(R+'/docs/spec/conformance-plan.md').read()
missing=[c for c in sorted(defs) if c not in conf]
print('defined',len(defs),'with obligation',len(defs)-len(missing))
print('no obligation',missing)
```

### 9a.5 `shortaudit.py` — shorthand detection

Matches a backticked four-digit token not preceded by `ARA-NS-`. Run against
`docs/spec/*` only, since `docs/progress/*` legitimately quotes historical
shorthand. Result after the rewrite: **0** in the normative files.

```python
import re,glob,os
R=os.environ['REPO']
sh=re.compile(r'(?<![\w-])`(\d{4})`')
for f in glob.glob(R+'/docs/spec/*.md'):
    for i,line in enumerate(open(f),1):
        for m in sh.finditer(line):
            print(os.path.basename(f),i,m.group(1))
```

### 9a.6 `parsecfg.py` — grammar-driven example verification

A table-driven LL(1) recursive-descent parser built from the same FIRST/FOLLOW
computation, plus a tokenizer for the Arara subset. Used to check that every
documented example is derivable. Result: of 11 `arara` blocks, the 9 labelled
*Valid* and the 1 labelled *Runtime behavior* parse; the 1 labelled
`ARA-PARSE-0108` invalid is rejected; the semantic-error examples parse (they
must, since their defect is not syntactic). 0 unexpected outcomes.

The parser must be written so `Program` consumes `EOF` itself and the caller
compares the cursor to the token count rather than peeking again — peeking after
`EOF` is consumed raises `IndexError`, which is a bug in the harness and not a
finding about the grammar.

## 9b. Java 21 keyword inventory

The inventory must come from the specification text for the **matching** release.
Extracted from `https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html`
§3.9, then counted mechanically.

```python
import re
s=open('jls-3.html',encoding='utf-8',errors='replace').read()
sec=s[s.find('<a name="jls-3.9">'):s.find('<a name="jls-3.10">')]
txt=re.sub(r'<[^>]+>',' ',sec)
res=txt.split('ReservedKeyword:')[1].split('ContextualKeyword:')[0]
ctx=txt.split('ContextualKeyword:')[1].split('The keywords const')[0]
print(len(re.findall(r'[A-Za-z_][A-Za-z0-9_]*',res)))          # 52, including the gloss word
print(len(re.findall(r'[A-Za-z_][A-Za-z0-9_\-]*',ctx)))        # 17
```

**This count is the trap that produced the wrong answer twice.** The raw regex
returns 52 because the reserved-keyword block ends with `_ (underscore)`, and the
gloss word `underscore` matches the identifier pattern. The specification states
the count in prose: *"51 character sequences ... are reserved for use as keywords
and cannot be used as identifiers"*. After removing the gloss word the list is
**51**, and that is the correct figure. The authoritative check is the prose
count, not the regex.

## 9c. Naming probes (all five namespaces)

Each probe has a control that compiles cleanly. Without the control the result is
meaningless, which is how the D001-R2 five-name claim went wrong.

### 9c.1 JVM accepts every keyword as a class name

Generate one class file per name with a minimal writer, then define each under
`-Xverify:all` with a loader whose parent is the platform loader.

```java
static class Bytes extends ClassLoader {
  Bytes(){ super(ClassLoader.getPlatformClassLoader()); }
  Class<?> def(String n, byte[] b){ return defineClass(n,b,0,b.length); }
}
```

Result: **68 accepted, 0 rejected** for the 51 reserved plus 17 contextual names.
`true`, `false`, and `null` are also accepted (3 more).

The class-file writer needs four things right, and each wrong version produced a
`ClassFormatError` that looks like a JVM restriction but is not:

1. the constant pool must be complete before the header is serialized — building
   entries lazily while emitting methods truncates the pool;
2. a `Code` attribute is `attribute_name_index, attribute_length(4), max_stack,
   max_locals, code_length(4), code, exception_table_length, attributes_count`;
3. the class file has `fields_count` before `methods_count`; omitting an empty
   `fields_count` shifts every later field;
4. `<init>` must carry `ACC_PUBLIC` only. `ACC_PUBLIC|ACC_STATIC` (`0x9`) is
   rejected with *"Method &lt;init&gt; has illegal modifiers"*.

### 9c.2 ~~`javac` rejects every keyword as a class name~~ — INVALID, superseded by §9d

> **Invalid experiment.** This block writes every candidate to `t.java`. Java
> requires a public top-level class to match its file name, so the compile fails
> regardless of whether the candidate is legal. The procedure has no control and
> its 71-of-71 result is an artifact. §9d replaces it and obtains the opposite
> result for 11 contextual keywords. The listing is retained only to document the
> defect.

```sh
while read -r nm; do
  [ -z "$nm" ] && continue
  printf 'public class %s { }\n' "$nm" > t.java
  javac -d out t.java 2>/dev/null && echo "ACCEPT $nm" || echo "REJECT $nm"
done < all68.txt
```

Original (wrong) result: "0 accepted, 68 rejected". Corrected result: §9d.3.

### 9c.3 Whitespace package prohibition is enforced independently

A class whose internal name begins `java/` cannot be defined; this is
`defineClass`'s `IllegalName` check, not the verifier.

```text
REJECTED java/evil -> NoClassDefFoundError: IllegalName: java/evil
REJECTED java/lang/Evil -> NoClassDefFoundError: IllegalName: java/lang/Evil
```

Arara has no packages, so no unit can reach this namespace.

### 9c.4 Shadowing probes, each with a control

Fixtures live in separate directories so the shadow classes cannot leak onto the
control classpath. The control compiles the identical Java source with no shadow
class present.

| Probe | Control | With shadow class | Verdict |
| --- | --- | --- | --- |
| `String` | compiles | `incompatible types: java.lang.String cannot be converted to String` | breaks unqualified use |
| `System` | compiles | `cannot find symbol: variable out` | breaks unqualified use |
| `Object` | compiles | `incompatible types: Integer cannot be converted to Object` | breaks unqualified use |
| `Math` | compiles | `cannot find symbol: method abs(int)` | breaks unqualified use |
| `java` with `java.lang.String s` | compiles | `cannot find symbol: class lang` | breaks **qualified** use only |
| `java` with unqualified `String` | compiles | compiles | no visible effect |

The last two rows are why §1.5a is a table and not a sentence. The D001-R2 claim
that `java` breaks `java.lang.String` was true only for the qualified form.

## 9d. FIRST/FOLLOW table comparison

Compares the 20 published rows of `syntax.ebnf` §6.1 against the computed sets.
Cells must be parsed by extracting backticked symbols verbatim, because a naive
comma split destroys `` `,` `` and turns a matching row into a false mismatch.
The prose cells `expression-start` and `binary operators` stand for sets and must
be expanded before comparison.

Result: **20 rows checked, 0 discrepancies.**

## 10. Provenance recovery for the archived reports

The two delivered reports were never committed. They were recovered from the
local Kilo session store and archived verbatim:

```sh
sqlite3 ~/.local/share/kilo/kilo.db \
  "SELECT id,title,model,directory FROM session WHERE directory LIKE '%arara%'"
```

| Session | Title | Model | Role |
| --- | --- | --- | --- |
| `ses_ea6c0cce2ffeR49TVy54TkC3YC` | Arara JVM language bootstrap and design | robin / deepseek-v4.1-flash-1017 / max | D001 execution + report |
| `ses_ea6516f44ffeH7S27xXMICUPMc` | Arara naming decision and doc audit | hive / deepseek-v4.1-flash-1017 / max | D001-R1 + report |
| `ses_ea671da23ffePidL0UmC1IyVvn` | plan session | hive | recovery run 1 |
| `ses_ea6712605ffed9g8Kp4OK040Kf` | plan session | hive | recovery run 2 |
| `ses_ea6834de1ffeZvOhujCBH5cgL6` | Audit D001 specification docs | general subagent | independent read-only audit |

Message text was extracted from the `part` table where `type = 'text'`. No
content was edited; only the surrounding provenance header was added.

## 9d. Directive 001-R4 — corrected `javac` class-name experiment

### 9d.1 Why the earlier experiment was invalid

The D001-R3 probe wrote every candidate source to `/tmp/…/t.java` while declaring
`public class <candidate> {}`. Java requires a public top-level class to match its
file name, so `javac` failed identically for every candidate. That procedure has
no control: it cannot distinguish `class` from `Foo`, and its "71 of 71 rejected"
conclusion was an artifact of the harness, not a property of the language.

### 9d.2 Corrected procedure

Each candidate is written to `<candidate>.java` and compiled in its own directory.
Five ordinary identifiers are compiled with the same harness as positive controls.
The candidate lists are the 51 reserved keywords, the 17 contextual keywords, the
three literals, and the five controls.

```bash
#!/bin/bash
RESERVED="abstract assert boolean break byte case catch char class const continue default do double else enum extends final finally float for goto if implements import instanceof int interface long native new package private protected public return short static strictfp super switch synchronized this throw throws transient try void volatile while _"
CONTEXT="exports opens requires to uses provides with transitive open module var yield record sealed permits non-sealed when"
LITERALS="true false null"
CONTROLS="Arara Foo _arara AraraLang x1"

test_one() {
  name="$1"; group="$2"
  d="/tmp/arara-r4/kw/work/$group/$name"
  rm -rf "$d"; mkdir -p "$d"
  printf 'public class %s {}\n' "$name" > "$d/$name.java"
  out=$(cd "$d" && javac "$name.java" 2>&1); rc=$?
  echo "$group|$name|$rc|$(echo "$out" | head -1)"
}
for n in $RESERVED; do test_one "$n" reserved; done
for n in $CONTEXT;  do test_one "$n" contextual; done
for n in $LITERALS; do test_one "$n" literal; done
for n in $CONTROLS; do test_one "$n" control; done
```

Environment: `javac 21.0.12.1`, OpenJDK 64-Bit Server VM Corretto-21.0.12.12.1,
macOS arm64.

### 9d.3 Result

Reproduced in the Directive 001-R5 session on `javac 21.0.12.1` (Corretto
21.0.12.12.1, macOS arm64) with the same harness and the same five compiling
controls:

| Group | n | Exit 0 | Exit 1 |
| --- | --- | --- | --- |
| Reserved keywords | 51 | 0 | **51** |
| Contextual keywords | 17 | **11** | **6** |
| Literals `true`/`false`/`null` | 3 | 0 | **3** |
| Controls | 5 | **5** | **0** |
| **Total** | **76** | **16** | **60** |

Of the 76 candidates, 60 are rejected and 16 are accepted. Removing the 5
controls leaves the 71 Arara-list names: **60 rejected + 11 accepted = 71**.
The boundary is checked programmatically over the raw `group|name|status` rows,
not by eye:

```python
import collections
rows = [l.strip().split('|') for l in open('out.txt') if l.strip()]
groups = collections.defaultdict(lambda: [0, 0])
for group, name, rc in rows:
    assert rc in ('0', '1')
    groups[group][0 if rc == '0' else 1] += 1
cands = [r for r in rows if r[0] != 'control']
accepted = sum(1 for r in cands if r[2] == '0')
rejected = sum(1 for r in cands if r[2] == '1')
assert accepted + rejected == len(cands) == 71
assert accepted == 11 and rejected == 60
assert groups['control'] == [5, 0]
```

Observed output of that check in the D001-R5 session:

```text
group counts (accepted, rejected):
  reserved      0  51  n=51
  contextual   11   6  n=17
  literal       0   3  n=3
  control       5   0  n=5
candidates = 71 accepted = 11 rejected = 60
ASSERTIONS PASSED: accepted + rejected == candidates == 71; 11 + 60
accepted contextual names: exports opens requires to uses provides with transitive open module when
rejected contextual names: var yield record sealed permits non-sealed
```

The 6 rejected contextual keywords are `var`, `yield`, `record`, `sealed`,
`permits` (`'…' not allowed here`) and `non-sealed` (`'{' expected`; also outside
the §1.2b rule-1 pattern because of `-`).

The 11 accepted contextual keywords are:

```text
exports opens requires to uses provides with transitive open module when
```

Representative diagnostics, quoted verbatim:

```text
abstract.java:1: error: <identifier> expected
assert.java:1: error: as of release 1.4, 'assert' is a keyword, and may not be used as an identifier
_ .java:1: error: as of release 9, '_' is a keyword, and may not be used as an identifier
var.java:1: error: 'var' not allowed here
module.java: (no output; exit 0)
open.java: (no output; exit 0)
```

Conclusion: `javac` refuses **60** of the 71 names in the Arara list — the 51
reserved keywords, `true`/`false`/`null`, and 6 of the 17 contextual keywords
(`var`, `yield`, `record`, `sealed`, `permits`, `non-sealed`). The remaining 11
are excluded by Arara policy (`semantics.md` §1.2b rule 2), not by `javac`.

**Correction, Directive 001-R5:** D001-R4 reported 59 rejected. That figure
subtracted `non-sealed` on the grounds that §1.2b rule 1 rejects it on the pattern
before rule 2 runs. The subtraction is true of the *rule order* but false of the
*experiment*: `non-sealed` was one of the 71 candidates actually compiled, and
`javac` rejected it. Reporting it as untested broke the invariant
accepted + rejected = candidates. The count is now 60/11, the experiment was rerun
in the D001-R5 session, and the rule-order note
is stated separately so the two facts cannot be conflated again.

## 9e. Directive 001-R4 — JVM internal names and shadowing

### 9e.1 Unit `String` emits `String`, not `java/lang/String`

```bash
mkdir -p pkg && cd pkg
printf 'public class String { public static java.lang.String make(){ return "abc"; } }\n' > String.java
javac String.java          # exit 0
javap -v String.class | grep -E 'this_class|super_class|major'
```

Verbatim output of the relevant lines:

```text
  minor version: 0
  major version: 65
  this_class: #9                          // String
  super_class: #2                         // java/lang/Object
```

`this_class` is `String`. The claim that a unit named `String` produces an internal
name such as `java/lang/String` is false and contradicted `semantics.md` §11.2.

### 9e.2 Both names coexist in one class file and run

```bash
printf 'public class Main {\n  public static void main(java.lang.String[] a) {\n    java.lang.String x = "hi";\n    java.lang.System.out.println(x + "/" + String.mk() + "/" + x.length());\n  }\n}\n' > Main.java
javac String.java Main.java && java -Xverify:all Main
```

Output, exit status 0:

```text
hi/s/2
```

Constant-pool entries in the same class:

```text
  #15 = Methodref   #16.#17   // String.mk:()Ljava/lang/String;
  #16 = Class       #18       // String
  #21 = Methodref   #22.#23   // java/lang/String.length:()I
  #22 = Class       #24       // java/lang/String
```

The generated unit class and the JDK type are distinct entries. References to the
Arara `String` type remain `Ljava/lang/String;` because that type *is*
`java.lang.String`.

### 9e.3 `defineClass` distinguishes the two names

```java
static class L extends ClassLoader {
  Class<?> def(String n, byte[] b) { return defineClass(n, b, 0, b.length); }
}
```

Feeding the same `String.class` bytes under different names:

```text
OK    String            -> String
FAIL  java.lang.String  -> SecurityException: Prohibited package name: java.lang
FAIL  java/lang/String  -> NoClassDefFoundError: IllegalName: java/lang/String
```

So `String` is a legal class name and `java/lang/String` is not. Shadowing cannot
be a bytecode or verifier problem.

### 9e.4 Java-source shadowing, each with a compiling control

Class files for `String`, `System`, `Object`, `Math`, and `java` were produced with
`javac` in a directory used as a classpath; each probe was then compiled twice,
once against that directory and once without it.

| Probe source | With shadowing class | Control (no shadowing) |
| --- | --- | --- |
| `String s = "abc";` | `incompatible types` | compiles |
| `System.out.println(1);` | `cannot find symbol: variable out` | compiles |
| `Object o = Integer.valueOf(1);` | `incompatible types` | compiles |
| `Math.abs(-2)` | `cannot find symbol: method abs(int)` | compiles |
| `java.lang.String s = "a";` | `cannot find symbol: class lang` | compiles |
| `String s = "a";` (only `java` shadowed) | compiles | compiles |

The last row is why the rule is stated per name rather than as a general
prohibition: a unit named `java` affects only fully qualified `java.` references.
