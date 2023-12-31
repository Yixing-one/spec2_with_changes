package org.specs2.fp

/** Inspired from the scalaz (https://github.com/scalaz/scalaz) project
  */
trait Foldable[F[_]]:

  /** Map each element of the structure to a Monoid, and combine the results. */
  def foldMap[A, B](fa: F[A])(f: A => B)(using F: Monoid[B]): B

  /** Right-associative fold of a structure. */
  def foldRight[A, B](fa: F[A], z: =>B)(f: (A, =>B) => B): B

  /** Left-associative fold of a structure. */
  def foldLeft[A, B](fa: F[A], z: B)(f: (B, A) => B): B

  /** Right-associative, monadic fold of a structure. */
  def foldRightM[G[_], A, B](fa: F[A], z: =>B)(f: (A, =>B) => G[B])(using M: Monad[G]): G[B] =
    foldLeft[A, B => G[B]](fa, M.point(_))((b, a) => w => M.bind(f(a, w))(b))(z)

  /** Left-associative, monadic fold of a structure. */
  def foldLeftM[G[_], A, B](fa: F[A], z: B)(f: (B, A) => G[B])(using M: Monad[G]): G[B] =
    foldRight[A, B => G[B]](fa, M.point(_))((a, b) => w => M.bind(f(w, a))(b))(z)

  /** Specialization of foldRightM when `B` has a `Monoid`. */
  def foldMapM[G[_], A, B](fa: F[A])(f: A => G[B])(using B: Monoid[B], G: Monad[G]): G[B] =
    foldRightM[G, A, B](fa, B.zero)((a, b2) => G.map(f(a))(b1 => B.append(b1, b2)))

  /** Combine the elements of a structure using a monoid. */
  def fold[M: Monoid](t: F[M]): M = foldMap[M, M](t)(x => x)

  /** Strict traversal in an applicative functor `M` that ignores the result of `f`. */
  def traverse_[M[_], A, B](fa: F[A])(f: A => M[B])(using a: Applicative[M]): M[Unit] =
    foldLeft(fa, a.pure(()))((x, y) => a.ap(f(y))(a.map(x)(_ => _ => ())))

  /** Strict sequencing in an applicative functor `M` that ignores the value in `fa`. */
  def sequence_[M[_], A](fa: F[M[A]])(using a: Applicative[M]): M[Unit] =
    traverse_(fa)(x => x)

  def findLeft[A](fa: F[A])(f: A => Boolean): Option[A] =
    foldLeft[A, Option[A]](fa, None)((b, a) => b.orElse(if f(a) then Some(a) else None))

  def findRight[A](fa: F[A])(f: A => Boolean): Option[A] =
    foldRight[A, Option[A]](fa, None)((a, b) => b.orElse(if f(a) then Some(a) else None))

  /** Alias for `length`. */
  final def count[A](fa: F[A]): Int = length(fa)

  /** Deforested alias for `toStream(fa).size`. */
  def length[A](fa: F[A]): Int = foldLeft(fa, 0)((b, _) => b + 1)

  /** @return
    *   the element at index `i` in a `Some`, or `None` if the given index falls outside of the range
    */
  def index[A](fa: F[A], i: Int): Option[A] =
    foldLeft[A, (Int, Option[A])](fa, (0, None)) { case ((idx, elem), curr) =>
      (idx + 1, elem orElse { if idx == i then Some(curr) else None })
    }._2

  /** @return
    *   the element at index `i`, or `default` if the given index falls outside of the range
    */
  def indexOr[A](fa: F[A], default: =>A, i: Int): A =
    index(fa, i) getOrElse default

  def toList[A](fa: F[A]): List[A] = foldLeft(fa, scala.List[A]())((t, h) => h :: t).reverse
  def toVector[A](fa: F[A]): Vector[A] = foldLeft(fa, Vector[A]())(_ :+ _)
  def toSet[A](fa: F[A]): Set[A] = foldLeft(fa, Set[A]())(_ + _)
  def toStream[A](fa: F[A]): LazyList[A] = foldRight[A, LazyList[A]](fa, LazyList.empty)(LazyList.cons(_, _))

  /** Whether all `A`s in `fa` yield true from `p`. */
  def all[A](fa: F[A])(p: A => Boolean): Boolean = foldRight(fa, true)(p(_) && _)

  /** `all` with monadic traversal. */
  def allM[G[_], A](fa: F[A])(p: A => G[Boolean])(using G: Monad[G]): G[Boolean] =
    foldRight(fa, G.point(true))((a, b) => G.bind(p(a))(q => if q then b else G.point(false)))

  /** Whether any `A`s in `fa` yield true from `p`. */
  def any[A](fa: F[A])(p: A => Boolean): Boolean = foldRight(fa, false)(p(_) || _)

  /** `any` with monadic traversal. */
  def anyM[G[_], A](fa: F[A])(p: A => G[Boolean])(using G: Monad[G]): G[Boolean] =
    foldRight(fa, G.point(false))((a, b) => G.bind(p(a))(q => if q then G.point(true) else b))

  def sumr[A](fa: F[A])(using A: Monoid[A]): A =
    foldRight(fa, A.zero)(A.append)

  def suml[A](fa: F[A])(using A: Monoid[A]): A =
    foldLeft(fa, A.zero)(A.append(_, _))

  /** Deforested alias for `toStream(fa).isEmpty`. */
  def empty[A](fa: F[A]): Boolean = all(fa)(_ => false)

  /** Insert an `A` between every A, yielding the sum. */
  def intercalate[A](fa: F[A], a: A)(using A: Monoid[A]): A =
    foldRight(fa, None: Option[A]) { (l, oa) =>
      Some(A.append(l, oa map (A.append(a, _)) getOrElse A.zero))
    }.getOrElse(A.zero)

object Foldable:
  @inline def apply[F[_]](using F: Foldable[F]): Foldable[F] = F

  given Foldable[List] with
    def foldMap[A, B](fa: List[A])(f: A => B)(using F: Monoid[B]): B =
      fa.foldLeft(F.zero) { (res, cur) => F.append(res, f(cur)) }

    def foldRight[A, B](fa: List[A], z: =>B)(f: (A, =>B) => B): B =
      fa.foldRight(z) { (cur, res) => f(cur, res) }

    def foldLeft[A, B](fa: List[A], z: B)(f: (B, A) => B): B =
      fa.foldLeft(z)(f)

  given Foldable[Vector] with
    def foldMap[A, B](fa: Vector[A])(f: A => B)(using F: Monoid[B]): B =
      fa.foldLeft(F.zero) { (res, cur) => F.append(res, f(cur)) }

    def foldRight[A, B](fa: Vector[A], z: =>B)(f: (A, =>B) => B): B =
      fa.foldRight(z) { (cur, res) => f(cur, res) }

    def foldLeft[A, B](fa: Vector[A], z: B)(f: (B, A) => B): B =
      fa.foldLeft(z)(f)

  given Foldable[LazyList] with
    def foldMap[A, B](fa: LazyList[A])(f: A => B)(using F: Monoid[B]): B =
      fa.foldLeft(F.zero) { (res, cur) => F.append(res, f(cur)) }

    def foldRight[A, B](fa: LazyList[A], z: =>B)(f: (A, =>B) => B): B =
      fa.foldRight(z) { (cur, res) => f(cur, res) }

    def foldLeft[A, B](fa: LazyList[A], z: B)(f: (B, A) => B): B =
      fa.foldLeft(z)(f)

trait FoldableSyntax:

  extension [F[_]: Foldable, A](fa: F[A])
    def toList: List[A] =
      Foldable[F].toList(fa)

  extension [F[_]: Foldable, A: Monoid](fa: F[A])

    def sumr: A =
      Foldable[F].sumr(fa)

    def suml: A =
      Foldable[F].suml(fa)

  extension [F[_]: Foldable, M[_]: Applicative, A, B](fa: F[A])
    def traverse_(f: A => M[B]): M[Unit] =
      Foldable[F].traverse_(fa)(f)

  extension [A, B, F[_]: Foldable, M[_]: Monad](fa: F[A])

    def foldLeftM(z: B)(f: (B, A) => M[B]): M[B] =
      Foldable[F].foldLeftM(fa, z)(f)

    def foldRightM(z: B)(f: (A, =>B) => M[B]): M[B] =
      Foldable[F].foldRightM(fa, z)(f)

  extension [F[_]: Foldable, A, B](fa: F[A])

    def foldMap(f: A => B)(using Monoid[B]): B =
      Foldable[F].foldMap(fa)(f)

    def foldLeft(z: B)(f: (B, A) => B): B =
      Foldable[F].foldLeft(fa, z)(f)

  extension [F[_]: Foldable, A: Monoid](fa: F[A])
    def sumAll: A =
      Foldable[F].foldMap(fa)(identity)
