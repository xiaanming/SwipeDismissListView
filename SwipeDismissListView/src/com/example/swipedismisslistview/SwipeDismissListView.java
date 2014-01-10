package com.example.swipedismisslistview;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
/**
 * @blog http://blog.csdn.net/xiaanming
 * 
 * @author xiaanming
 *
 */
public class SwipeDismissListView extends ListView {
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	protected long mAnimationTime;
	private boolean mSwiping;
	private VelocityTracker mVelocityTracker;
	private int mDownPosition;
	private View mDownView;
	private float mDownX;
	private float mDownY;
	private int mViewWidth;
	private OnDismissCallback onDismissCallback;

	/**
	 * ���ö���ʱ��
	 * 
	 * @param mAnimationTime
	 */
	public void setmAnimationTime(long mAnimationTime) {
		this.mAnimationTime = mAnimationTime;
	}

	/**
	 * ����ɾ���ص��ӿ�
	 * 
	 * @param onDismissCallback
	 */
	public void setOnDismissCallback(OnDismissCallback onDismissCallback) {
		this.onDismissCallback = onDismissCallback;
	}

	public SwipeDismissListView(Context context) {
		this(context, null);
	}

	public SwipeDismissListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeDismissListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		ViewConfiguration vc = ViewConfiguration.get(context);
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 8; //��ȡ��������С�ٶ�
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();  //��ȡ����������ٶ�
		mAnimationTime = context.getResources().getInteger(
				android.R.integer.config_shortAnimTime);
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handleActionDown(ev);
			break;
		case MotionEvent.ACTION_MOVE:
			return handleActionMove(ev);
		case MotionEvent.ACTION_UP:
			handleActionUp(ev);
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * �����¼�����
	 * 
	 * @param ev
	 * @return
	 */
	private void handleActionDown(MotionEvent ev) {
		mDownX = ev.getX();
		mDownY = ev.getY();
		
		mDownPosition = pointToPosition((int) mDownX, (int) mDownY);

		if (mDownPosition == AdapterView.INVALID_POSITION) {
			return;
		}

		mDownView = getChildAt(mDownPosition - getFirstVisiblePosition());

		if (mDownView != null) {
			mViewWidth = mDownView.getWidth();
		}

		//�����ٶȼ��
		mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(ev);
	}

	/**
	 * ������ָ�����ķ���
	 * 
	 * @param ev
	 * @return
	 */
	private boolean handleActionMove(MotionEvent ev) {
		if (mVelocityTracker == null || mDownView == null) {
			return super.onTouchEvent(ev);
		}

		// ��ȡX���򻬶��ľ���
		float deltaX = ev.getX() - mDownX;
		float deltaY = ev.getY() - mDownY;

		// X���򻬶��ľ������mSlop����Y���򻬶��ľ���С��mSlop����ʾ���Ի���
		if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < mSlop) {
			mSwiping = true;
			
			//����ָ����item,ȡ��item�ĵ���¼�����Ȼ���ǻ���ItemҲ������item����¼��ķ���
			MotionEvent cancelEvent = MotionEvent.obtain(ev);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                       (ev.getActionIndex()<< MotionEvent.ACTION_POINTER_INDEX_SHIFT));
            onTouchEvent(cancelEvent);
		}

		if (mSwiping) {
			// ��˭��ָ�ƶ�item
			setTranslationX(mDownView, deltaX);
			// ͸���Ƚ���
			setAlpha(mDownView,
					Math.max(
							0f,
							Math.min(1f, 1f - 2f * Math.abs(deltaX)/ mViewWidth)));

			// ��ָ������ʱ��,����true����ʾSwipeDismissListView�Լ�����onTouchEvent,�����ľͽ�������������
			return true;
		}

		return super.onTouchEvent(ev);

	}

	/**
	 * ��ָ̧����¼�����
	 * @param ev
	 */
	private void handleActionUp(MotionEvent ev) {
		if (mVelocityTracker == null || !mSwiping) {
			return;
		}

		float deltaX = ev.getX() - mDownX;
		
		//ͨ�������ľ�������X,Y������ٶ�
		mVelocityTracker.computeCurrentVelocity(1000);
		float velocityX = Math.abs(mVelocityTracker.getXVelocity());
		float velocityY = Math.abs(mVelocityTracker.getYVelocity());
		
		boolean dismiss = false;
		boolean dismissRight = false;//�Ƿ����ұ�ɾ��
		if (Math.abs(deltaX) > mViewWidth / 2) {
			dismiss = true;
			dismissRight = deltaX > 0;
		} else if (mMinFlingVelocity <= velocityX
				&& velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
			dismiss = true;
			dismissRight = mVelocityTracker.getXVelocity() > 0;
		}

		if (dismiss) {
			animate(mDownView)
					.translationX(dismissRight ? mViewWidth : -mViewWidth)//X�᷽����ƶ�����
					.alpha(0)
					.setDuration(mAnimationTime)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							//Item��������֮��ִ��ɾ��
							performDismiss(mDownView, mDownPosition);
						}
					});
		} else {
			animate(mDownView)
			.translationX(0)
			.alpha(1)	
			.setDuration(mAnimationTime).setListener(null);
		}
		
		//�Ƴ��ٶȼ��
		if(mVelocityTracker != null){
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
		
		mSwiping = false;
	}

	
	/**
	 * �ڴ˷�����ִ��itemɾ��֮��������item���ϻ������¹����Ķ��������ҽ�position�ص�������onDismiss()��
	 * @param dismissView
	 * @param dismissPosition
	 */
	private void performDismiss(final View dismissView, final int dismissPosition) {
		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
		final int originalHeight = dismissView.getHeight();

		//
		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(mAnimationTime);
		animator.start();

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (onDismissCallback != null) {
					onDismissCallback.onDismiss(dismissPosition);
				}

				//��δ������Ҫ����Ϊ���ǲ�û�н�item��ListView���Ƴ������ǽ�item�ĸ߶�����Ϊ0
				setAlpha(dismissView, 1f);
				setTranslationX(dismissView, 0);
				ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
				lp.height = originalHeight;
				dismissView.setLayoutParams(lp);

			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				//��δ����Ч����ListViewɾ��ĳitem֮��������item���ϻ�����Ч��
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});

	}

	/**
	 * ɾ���Ļص��ӿ�
	 * 
	 * @author xiaanming
	 * 
	 */
	public interface OnDismissCallback {
		public void onDismiss(int dismissPosition);
	}

}
