package com.cramsan.petproject.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.cramsan.framework.logging.EventLoggerInterface
import com.cramsan.framework.logging.Severity
import com.cramsan.framework.logging.classTag
import com.cramsan.petproject.PetProjectApplication
import org.kodein.di.KodeinAware
import org.kodein.di.erased.instance

abstract class BaseFragment : Fragment(), KodeinAware {

    override val kodein by lazy { (requireActivity().application as PetProjectApplication).kodein }
    protected val eventLogger: EventLoggerInterface by instance()

    abstract val contentViewLayout: Int

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        eventLogger.log(Severity.INFO, classTag(), "onAttach")
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventLogger.log(Severity.INFO, classTag(), "onCreate")
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        eventLogger.log(Severity.INFO, classTag(), "onCreateView")
        return inflater.inflate(contentViewLayout, container, false)
    }

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventLogger.log(Severity.INFO, classTag(), "onActivityCreated")
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        eventLogger.log(Severity.INFO, classTag(), "onStart")
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        eventLogger.log(Severity.INFO, classTag(), "onResume")
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        eventLogger.log(Severity.INFO, classTag(), "onPause")
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        eventLogger.log(Severity.INFO, classTag(), "onStop")
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        eventLogger.log(Severity.INFO, classTag(), "onDestroyView")
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        eventLogger.log(Severity.INFO, classTag(), "onDestroy")
    }

    @CallSuper
    override fun onDetach() {
        super.onDetach()
        eventLogger.log(Severity.INFO, classTag(), "onDetach")
    }
}
